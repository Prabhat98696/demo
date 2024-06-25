package com.lcs.wc.client.web.html;

import com.lcs.wc.client.web.FormGenerator;
import com.lcs.wc.client.web.SearchFormGenerator2;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RB;
import java.util.Map;
import wt.util.WTException;
import wt.util.WTMessage;

public class AgronDefaultSequenceAttributeRenderer extends DefaultCommonAttributeRenderer implements SequenceAttributeRenderer {
  public String createSequenceDisplay(String label, String formName, String value, boolean create, String elementId) {
    StringBuilder builder = new StringBuilder();
    String format = "STRING_FORMAT";
    if (!FormatHelper.hasContent(value)) {
      if (create)
        builder.append(FormGenerator.createHiddenInput(formName, " ")); 
      value = WTMessage.getLocalizedMessage("com.lcs.wc.resource.FlexGeneratorRB", "nextSequenceValue_LBL", RB.objA);
      format = "STRING_FORMAT";
    } else if (create) {
      builder.append(FormGenerator.createHiddenInput(formName, value));
    }  
    DisplayElement sequenceDisplay = new DisplayElement(label, value);
    sequenceDisplay.setFormat(format);
    sequenceDisplay.setId(elementId);
    builder.append(createDisplay(sequenceDisplay));
    return builder.toString();
  }
  
  public String drawDisplay(FlexTypeAttribute att, FlexTyped typed, boolean valueOnly, String stringValue, String label) throws WTException {
    StringBuilder builder = new StringBuilder();
    String elementId = FormatHelper.hasContent(att.getAttDerivedFrom()) ? null : att.getAttKey();
    if (typed != null && !valueOnly)
      stringValue = getStringValue(att, typed); 
    builder.append(createSequenceDisplay(label, (String)null, stringValue, false, elementId));
    return builder.toString();
  }
  
  public String drawFormWidget(FlexTypeAttribute att, FlexTyped typed, String labelOverride, boolean bodyOnly, String stringValue, String formNameOverride, HTMLFormOptions formOptions, boolean disabled) throws WTException {
    StringBuilder builder = new StringBuilder();
    String formName = getAttributeFormName(att, typed, formNameOverride, formOptions);
    String label = getLabel(att, typed, labelOverride, formOptions);
    boolean create = HTMLFormOptions.FormType.CREATE.equals(formOptions.getFormType());
    builder.append(createSequenceDisplay(label, formName, stringValue, create, (String)null));
    return builder.toString();
  }
  

//  public String createSearchCriteriaWidget(FlexTypeAttribute att, FlexType type, Map<String, String> criteria, String searchCriteriaIndexOverride, boolean useObjectReferenceChooser, String labelPrefix, HTMLFormOptions formOptions) throws WTException {

@Override
public String createSearchCriteriaWidget(FlexTypeAttribute att, FlexType type, Map criteria, String searchCriteriaIndexOverride, boolean useObjectReferenceChooser,String labelPrefix, HTMLFormOptions formOptions) throws WTException {
	  if (!ACLHelper.hasViewAccess(att))
	      return ""; 
	    boolean useConstraints = LCSProperties.getBoolean("com.lcs.wc.client.web.FlexTypeGenerator.createSearchCriteriaWidget.useIntegerConstraints");
	    String label = getSearchCriteriaLabel(att, labelPrefix);
	    String scIndex = getSearchCriteriaIndex(att, searchCriteriaIndexOverride, formOptions);
	    appendNumericSearchCriteriaValue(criteria, scIndex);
	    StringBuilder builder = new StringBuilder();
	    builder.append(
	        SearchFormGenerator2.createNumRangeSearchWidget(att.getAttVariableType(), label, scIndex, att
	          .getAttLowerLimit(), att.getAttUpperLimit(), false, useConstraints, criteria));
	    return builder.toString();
}
}
