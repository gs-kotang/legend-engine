// Copyright 2021 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.external.shared.runtime;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.external.shared.runtime.read.ExecutionHelper;
import org.finos.legend.engine.external.shared.utils.ExternalFormatRuntime;
import org.finos.legend.engine.plan.dependencies.domain.dataQuality.BasicChecked;
import org.finos.legend.engine.plan.dependencies.domain.dataQuality.Constrained;
import org.finos.legend.engine.plan.dependencies.domain.dataQuality.EnforcementLevel;
import org.finos.legend.engine.plan.dependencies.domain.dataQuality.IChecked;
import org.finos.legend.engine.plan.dependencies.domain.dataQuality.IDefect;
import org.finos.legend.engine.plan.execution.extension.ExecutionExtension;
import org.finos.legend.engine.plan.execution.nodes.ExecutionNodeExecutor;
import org.finos.legend.engine.plan.execution.nodes.helpers.freemarker.FreeMarkerExecutor;
import org.finos.legend.engine.plan.execution.nodes.state.ExecutionState;
import org.finos.legend.engine.plan.execution.result.InputStreamResult;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.result.object.StreamingObjectResult;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.externalFormat.DataQualityExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.externalFormat.ExternalFormatExternalizeExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.externalFormat.ExternalFormatInternalizeExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.externalFormat.UrlStreamExecutionNode;
import org.finos.legend.engine.shared.core.url.UrlFactory;
import org.pac4j.core.profile.CommonProfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ExternalFormatExecutionExtension implements ExecutionExtension
{
    private final Map<String, ExternalFormatRuntimeExtension> EXTENSIONS = ExternalFormatRuntimeExtensionLoader.extensions();

    @Override
    public List<Function3<ExecutionNode, MutableList<CommonProfile>, ExecutionState, Result>> getExtraNodeExecutors()
    {
        return Collections.singletonList((executionNode, pm, executionState) ->
        {
            if (executionNode instanceof DataQualityExecutionNode)
            {
                return executeDataQuality((DataQualityExecutionNode) executionNode, pm, executionState);
            }
            else if (executionNode instanceof UrlStreamExecutionNode)
            {
                return executeUrlStream((UrlStreamExecutionNode) executionNode, pm, executionState);
            }
            else if (executionNode instanceof ExternalFormatInternalizeExecutionNode)
            {
                return executeInternalizeExecutionNode((ExternalFormatInternalizeExecutionNode) executionNode, pm, executionState);
            }
            else if (executionNode instanceof ExternalFormatExternalizeExecutionNode)
            {
                return executeExternalizeExecutionNode((ExternalFormatExternalizeExecutionNode) executionNode, pm, executionState);
            }
            else
            {
                return null;
            }
        });
    }

    private Result executeInternalizeExecutionNode(ExternalFormatInternalizeExecutionNode node, MutableList<CommonProfile> profiles, ExecutionState executionState)
    {
        ExternalFormatRuntimeExtension extension = EXTENSIONS.get(node.contentType);
        if (extension == null)
        {
            throw new IllegalStateException("No runtime extension for contentType " + node.contentType);
        }

        InputStream stream = ExecutionHelper.inputStreamFromResult(node.executionNodes().getFirst().accept(new ExecutionNodeExecutor(profiles, new ExecutionState(executionState))));
        StreamingObjectResult<?> streamingObjectResult = extension.executeInternalizeExecutionNode(node, stream, profiles, executionState);
        return applyConstraints(streamingObjectResult, node.checked, node.enableConstraints);
    }

    private Result executeExternalizeExecutionNode(ExternalFormatExternalizeExecutionNode node, MutableList<CommonProfile> profiles, ExecutionState executionState)
    {
        ExternalFormatRuntimeExtension extension = EXTENSIONS.get(node.contentType);
        if (extension == null)
        {
            throw new IllegalStateException("No runtime extension for contentType " + node.contentType);
        }

        Result result = node.executionNodes().getAny().accept(new ExecutionNodeExecutor(profiles, executionState));
        return extension.executeExternalizeExecutionNode(node, result, profiles, executionState);
    }

    private Result executeUrlStream(UrlStreamExecutionNode node, MutableList<CommonProfile> profiles, ExecutionState executionState)
    {
        try
        {
            String url;
            if (node.requiredVariableInputs == null || node.requiredVariableInputs.isEmpty())
            {
                url = node.url;
            }
            else
            {
                url = FreeMarkerExecutor.process(node.url, executionState);
            }
            InputStream inputStream = UrlFactory.create(url).openStream();
            return new InputStreamResult(inputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Result executeDataQuality(DataQualityExecutionNode node, MutableList<CommonProfile> profiles, ExecutionState executionState)
    {
        ExecutionNode inputNode = node.executionNodes().getAny();
        Result input = inputNode.accept(new ExecutionNodeExecutor(profiles, executionState));
        StreamingObjectResult<?> streamingObjectResult = (StreamingObjectResult) input;
        return applyConstraints(streamingObjectResult, node.checked, node.enableConstraints);
    }

    private Result applyConstraints(StreamingObjectResult<?> streamingObjectResult, boolean checked, boolean enableConstraints)
    {
        Stream<IChecked<?>> checkedStream = (Stream<IChecked<?>>) streamingObjectResult.getObjectStream();
        Stream<IChecked<?>> withConstraints = enableConstraints
                ? checkedStream.map(this::applyConstraints)
                : checkedStream;
        if (checked)
        {
            return new StreamingObjectResult<>(withConstraints, streamingObjectResult.getResultBuilder(), streamingObjectResult);
        }
        else
        {
            Stream<?> objectStream = ExternalFormatRuntime.unwrapCheckedStream(withConstraints);
            return new StreamingObjectResult<>(objectStream, streamingObjectResult.getResultBuilder(), streamingObjectResult);
        }
    }

    private IChecked<?> applyConstraints(IChecked<?> checked)
    {
        Object value = checked.getValue();
        List<IDefect> constraintFailures = Collections.emptyList();
        if (value instanceof Constrained)
        {
            constraintFailures = ((Constrained) value).allConstraints();
        }
        if (constraintFailures.isEmpty())
        {
            return checked;
        }
        else
        {
            List<IDefect> allDefects = new ArrayList(checked.getDefects());
            allDefects.addAll(constraintFailures);
            return allDefects.stream().anyMatch(d -> d.getEnforcementLevel() == EnforcementLevel.Critical)
                    ? BasicChecked.newChecked(null, checked.getSource(), allDefects)
                    : BasicChecked.newChecked(checked.getValue(), checked.getSource(), allDefects);
        }
    }
}
