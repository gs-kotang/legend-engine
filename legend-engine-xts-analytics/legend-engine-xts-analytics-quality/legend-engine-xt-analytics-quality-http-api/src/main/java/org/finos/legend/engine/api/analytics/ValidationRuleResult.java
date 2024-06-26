//  Copyright 2022 Goldman Sachs
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.finos.legend.engine.api.analytics;

public class ValidationRuleResult
{
    private boolean ruleResult;
    private String packageableElementName;
    private String violationType;
    private String errorMessage;
    private String ruleDescription;

    public ValidationRuleResult(boolean ruleResult, String packageableElementName, String violationType, String errorMessage, String ruleDescription)
    {
        this.ruleResult = ruleResult;
        this.packageableElementName = packageableElementName;
        this.violationType = violationType;
        this.errorMessage = errorMessage;
        this.ruleDescription = ruleDescription;
    }

    public void setRuleResult(boolean ruleResult)
    {
        this.ruleResult = ruleResult;
    }

    public void setPackageableElementName(String packageableElementName)
    {
        this.packageableElementName = packageableElementName;
    }

    public void setViolationType(String violationType)
    {
        this.violationType = violationType;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public void setRuleDescription(String ruleDescription)
    {
        this.ruleDescription = ruleDescription;
    }

    public boolean getRuleResult()
    {
        return this.ruleResult;
    }

    public String getPackageableElementName()
    {
        return this.packageableElementName;
    }

    public String getViolationType()
    {
        return this.violationType;
    }

    public String getErrorMessage()
    {
        return this.errorMessage;
    }

    public String getRuleDescription()
    {
        return this.ruleDescription;
    }

}
