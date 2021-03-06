/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.cmmn.editor.json.converter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.cmmn.editor.constants.CmmnStencilConstants;
import org.flowable.cmmn.model.BaseElement;
import org.flowable.cmmn.model.CmmnModel;
import org.flowable.cmmn.model.GenericEventListener;
import org.flowable.cmmn.model.PlanItem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 */
public class GenericEventListenerJsonConverter extends AbstractEventListenerJsonConverter {


    public static void fillTypes(Map<String, Class<? extends BaseCmmnJsonConverter>> convertersToCmmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseCmmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToCmmnMap);
        fillCmmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseCmmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_GENERIC_EVENT_LISTENER, GenericEventListenerJsonConverter.class);
    }

    public static void fillCmmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseCmmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(GenericEventListener.class, GenericEventListenerJsonConverter.class);
    }

    @Override
    protected void convertElementToJson(ObjectNode elementNode, ObjectNode propertiesNode, ActivityProcessor processor, BaseElement baseElement, CmmnModel cmmnModel) {
        convertCommonElementToJson(elementNode, propertiesNode, baseElement);

        GenericEventListener genericEventListener = (GenericEventListener) ((PlanItem) baseElement).getPlanItemDefinition();
        if (StringUtils.isNotEmpty(genericEventListener.getEventType())) {
            propertiesNode.put(PROPERTY_EVENT_TYPE, genericEventListener.getEventType());
        }
    }

    @Override
    protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, ActivityProcessor processor,
            BaseElement parentElement, Map<String, JsonNode> shapeMap, CmmnModel cmmnModel, CmmnJsonConverter.CmmnModelIdHelper cmmnModelIdHelper) {
        GenericEventListener genericEventListener = new GenericEventListener();
        convertCommonJsonToElement(elementNode, genericEventListener);

        String eventType = CmmnJsonConverterUtil.getPropertyValueAsString(PROPERTY_EVENT_TYPE, modelNode);
        if (StringUtils.isNotEmpty(eventType)) {
            genericEventListener.setEventType(eventType);
        }

        return genericEventListener;
    }

    @Override
    protected String getStencilId(BaseElement baseElement) {
        return CmmnStencilConstants.STENCIL_GENERIC_EVENT_LISTENER;
    }
}
