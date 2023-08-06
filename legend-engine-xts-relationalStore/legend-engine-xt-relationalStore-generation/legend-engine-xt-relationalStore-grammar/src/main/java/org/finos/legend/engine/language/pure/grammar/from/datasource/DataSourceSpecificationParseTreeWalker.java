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

package org.finos.legend.engine.language.pure.grammar.from.datasource;

import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParserUtility;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.connection.datasource.DataSourceSpecificationParserGrammar;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.specification.DatabricksDatasourceSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.specification.EmbeddedH2DatasourceSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.specification.LocalH2DatasourceSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.specification.RedshiftDatasourceSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.specification.StaticDatasourceSpecification;

public class DataSourceSpecificationParseTreeWalker
{
    public LocalH2DatasourceSpecification visitLocalH2DatasourceSpecification(DataSourceSpecificationSourceCode code, DataSourceSpecificationParserGrammar.LocalH2DatasourceSpecificationContext dbSpecCtx)
    {
        LocalH2DatasourceSpecification dsSpec = new LocalH2DatasourceSpecification();
        dsSpec.sourceInformation = code.getSourceInformation();
        // testDataSetupCsv
        DataSourceSpecificationParserGrammar.LocalH2DSPTestDataSetupCSVContext testDataSetupCSVCtx = PureGrammarParserUtility.validateAndExtractOptionalField(dbSpecCtx.localH2DSPTestDataSetupCSV(), "testDataSetupCsv", dsSpec.sourceInformation);
        if (testDataSetupCSVCtx != null)
        {
            dsSpec.testDataSetupCsv = PureGrammarParserUtility.fromGrammarString(testDataSetupCSVCtx.STRING().getText(), true);
        }
        if (dbSpecCtx.localH2DSPTestDataSetupSQLS() != null && !dbSpecCtx.localH2DSPTestDataSetupSQLS().isEmpty())
        {
            dsSpec.testDataSetupSqls = ListIterate.collect(dbSpecCtx.localH2DSPTestDataSetupSQLS().get(0).sqlsArray().STRING(), ctx -> PureGrammarParserUtility.fromGrammarString(ctx.getText(), true));
        }
        return dsSpec;
    }

    public EmbeddedH2DatasourceSpecification visitEmbeddedH2DatasourceSpecification(DataSourceSpecificationSourceCode code, DataSourceSpecificationParserGrammar.EmbeddedH2DatasourceSpecificationContext dbSpecCtx)
    {
        EmbeddedH2DatasourceSpecification dsSpec = new EmbeddedH2DatasourceSpecification();
        dsSpec.sourceInformation = code.getSourceInformation();
        // databaseName
        DataSourceSpecificationParserGrammar.DbNameContext databaseNameCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.dbName(), "name", dsSpec.sourceInformation);
        dsSpec.databaseName = PureGrammarParserUtility.fromGrammarString(databaseNameCtx.STRING().getText(), true);
        // directory
        DataSourceSpecificationParserGrammar.EmbeddedH2DSPDirectoryContext directoryCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.embeddedH2DSPDirectory(), "directory", dsSpec.sourceInformation);
        dsSpec.directory = PureGrammarParserUtility.fromGrammarString(directoryCtx.STRING().getText(), true);
        // autoServerMode
        DataSourceSpecificationParserGrammar.EmbeddedH2DSPAutoServerModeContext autoServerModeCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.embeddedH2DSPAutoServerMode(), "autoServerMode", dsSpec.sourceInformation);
        dsSpec.autoServerMode = Boolean.parseBoolean(autoServerModeCtx.BOOLEAN().getText());
        return dsSpec;
    }

    public DatabricksDatasourceSpecification visitDatabricksDatasourceSpecification(DataSourceSpecificationSourceCode code, DataSourceSpecificationParserGrammar.DatabricksDatasourceSpecificationContext dbSpecCtx)
    {
        DatabricksDatasourceSpecification dsSpec = new DatabricksDatasourceSpecification();
        dsSpec.sourceInformation = code.getSourceInformation();

        DataSourceSpecificationParserGrammar.HostnameContext hostnameCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.hostname(), "hostname", dsSpec.sourceInformation);
        dsSpec.hostname = PureGrammarParserUtility.fromGrammarString(hostnameCtx.STRING().getText(), true);

        DataSourceSpecificationParserGrammar.PortContext portCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.port(), "port", dsSpec.sourceInformation);
        dsSpec.port = PureGrammarParserUtility.fromGrammarString(portCtx.STRING().getText(), true);

        DataSourceSpecificationParserGrammar.ProtocolContext protocolCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.protocol(), "protocol", dsSpec.sourceInformation);
        dsSpec.protocol = PureGrammarParserUtility.fromGrammarString(protocolCtx.STRING().getText(), true);

        DataSourceSpecificationParserGrammar.HttpPathContext httpCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.httpPath(), "httpPath", dsSpec.sourceInformation);
        dsSpec.httpPath = PureGrammarParserUtility.fromGrammarString(httpCtx.STRING().getText(), true);

        return dsSpec;
    }

    public StaticDatasourceSpecification visitStaticDatasourceSpecification(DataSourceSpecificationSourceCode code, DataSourceSpecificationParserGrammar.StaticDatasourceSpecificationContext dbSpecCtx)
    {
        StaticDatasourceSpecification dsSpec = new StaticDatasourceSpecification();
        dsSpec.sourceInformation = code.getSourceInformation();
        // host
        DataSourceSpecificationParserGrammar.DbHostContext hostCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.dbHost(), "host", dsSpec.sourceInformation);
        dsSpec.host = PureGrammarParserUtility.fromGrammarString(hostCtx.STRING().getText(), true);
        // port
        DataSourceSpecificationParserGrammar.DbPortContext portCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.dbPort(), "port", dsSpec.sourceInformation);
        dsSpec.port = Integer.parseInt(portCtx.INTEGER().getText());
        // database name
        DataSourceSpecificationParserGrammar.DbNameContext nameCtx = PureGrammarParserUtility.validateAndExtractRequiredField(dbSpecCtx.dbName(), "name", dsSpec.sourceInformation);
        dsSpec.databaseName = PureGrammarParserUtility.fromGrammarString(nameCtx.STRING().getText(), true);
        return dsSpec;
    }

    public RedshiftDatasourceSpecification visitRedshiftDatasourceSpecification(DataSourceSpecificationSourceCode code, DataSourceSpecificationParserGrammar.RedshiftDatasourceSpecificationContext ctx)
    {
        RedshiftDatasourceSpecification redshiftSpec = new RedshiftDatasourceSpecification();
        DataSourceSpecificationParserGrammar.ClusterIDContext clusterID = PureGrammarParserUtility.validateAndExtractOptionalField(ctx.clusterID(), "clusterID", redshiftSpec.sourceInformation);
        DataSourceSpecificationParserGrammar.DbHostContext host = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.dbHost(), "dbHost", redshiftSpec.sourceInformation);
        DataSourceSpecificationParserGrammar.DbPortContext port = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.dbPort(), "port", redshiftSpec.sourceInformation);
        DataSourceSpecificationParserGrammar.RegionContext region = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.region(), "region", redshiftSpec.sourceInformation);
        DataSourceSpecificationParserGrammar.DbNameContext database = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.dbName(), "name", redshiftSpec.sourceInformation);
        DataSourceSpecificationParserGrammar.EndpointURLContext endpoint = PureGrammarParserUtility.validateAndExtractOptionalField(ctx.endpointURL(), "endpointURL", redshiftSpec.sourceInformation);

        redshiftSpec.clusterID = PureGrammarParserUtility.fromGrammarString(clusterID.STRING().getText(), true);
        redshiftSpec.host = PureGrammarParserUtility.fromGrammarString(host.STRING().getText(), true);
        redshiftSpec.port = Integer.parseInt(port.INTEGER().getText());
        redshiftSpec.region = PureGrammarParserUtility.fromGrammarString(region.STRING().getText(), true);
        redshiftSpec.databaseName = PureGrammarParserUtility.fromGrammarString(database.STRING().getText(), true);
        redshiftSpec.endpointURL = PureGrammarParserUtility.fromGrammarString(endpoint.STRING().getText(), true);

        return redshiftSpec;
    }
}
