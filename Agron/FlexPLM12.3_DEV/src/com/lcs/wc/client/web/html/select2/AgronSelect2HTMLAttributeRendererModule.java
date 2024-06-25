package com.lcs.wc.client.web.html.select2;

import com.google.inject.AbstractModule;
import com.lcs.wc.client.web.html.AgronDefaultSequenceAttributeRenderer;
import com.lcs.wc.client.web.html.BooleanAttributeRenderer;
import com.lcs.wc.client.web.html.ButtonRenderer;
import com.lcs.wc.client.web.html.CareWashAttributeRenderer;
import com.lcs.wc.client.web.html.CascadingAttributeRenderer;
import com.lcs.wc.client.web.html.ColorChoiceAttributeRenderer;
import com.lcs.wc.client.web.html.CompositeAttributeRenderer;
import com.lcs.wc.client.web.html.ConstantAttributeRenderer;
import com.lcs.wc.client.web.html.CurrencyAttributeRenderer;
import com.lcs.wc.client.web.html.DateAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultBooleanAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultButtonRenderer;
import com.lcs.wc.client.web.html.DefaultCareWashAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultCascadingAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultColorChoiceAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultCompositeAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultConstantAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultCurrencyAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultDateAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultDerivedNumberAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultDerivedStringAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultFloatAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultFormFieldRenderer;
import com.lcs.wc.client.web.html.DefaultIconButtonRenderer;
import com.lcs.wc.client.web.html.DefaultImageAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultIntegerAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultMultiEntryAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultMultiEntryOrderableAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultMultiSelectAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultObjectReferenceAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultObjectReferenceListAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultSequenceAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultSingleListAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultTextAreaAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultTextAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultUOMAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultURLAttributeRenderer;
import com.lcs.wc.client.web.html.DefaultUserListAttributeRenderer;
import com.lcs.wc.client.web.html.DerivedNumericAttributeRenderer;
import com.lcs.wc.client.web.html.DerivedStringAttributeRenderer;
import com.lcs.wc.client.web.html.FloatAttributeRenderer;
import com.lcs.wc.client.web.html.FormFieldRenderer;
import com.lcs.wc.client.web.html.IconButtonRenderer;
import com.lcs.wc.client.web.html.ImageAttributeRenderer;
import com.lcs.wc.client.web.html.IntegerAttributeRenderer;
import com.lcs.wc.client.web.html.MultiEntryAttributeRenderer;
import com.lcs.wc.client.web.html.MultiEntryOrderableAttributeRenderer;
import com.lcs.wc.client.web.html.MultiSelectAttributeRenderer;
import com.lcs.wc.client.web.html.ObjectReferenceAttributeRenderer;
import com.lcs.wc.client.web.html.ObjectReferenceListAttributeRenderer;
import com.lcs.wc.client.web.html.SequenceAttributeRenderer;
import com.lcs.wc.client.web.html.SingleListAttributeRenderer;
import com.lcs.wc.client.web.html.TextAreaAttributeRenderer;
import com.lcs.wc.client.web.html.TextAttributeRenderer;
import com.lcs.wc.client.web.html.UOMAttributeRenderer;
import com.lcs.wc.client.web.html.URLAttributeRenderer;
import com.lcs.wc.client.web.html.UserListAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2CareWashAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2CascadingAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2ColorChoiceAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2CompositeAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2MultiSelectAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2ObjectReferenceListAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2SingleListAttributeRenderer;
import com.lcs.wc.client.web.html.select2.Select2UserListAttributeRenderer;
import com.lcs.wc.client.web.image.DefaultObjectThumbnailPlugin;
import com.lcs.wc.client.web.image.ObjectThumbnailPlugin;
import com.lcs.wc.util.LCSProperties;

public class AgronSelect2HTMLAttributeRendererModule extends AbstractModule {
  static final int RENDER_MODE = Integer.parseInt(LCSProperties.get("com.lcs.wc.client.web.html.select2.renderMode", "1"));
  
  protected void configure() {
    bind(BooleanAttributeRenderer.class).to(DefaultBooleanAttributeRenderer.class);
    bind(ConstantAttributeRenderer.class).to(DefaultConstantAttributeRenderer.class);
    bind(CurrencyAttributeRenderer.class).to(DefaultCurrencyAttributeRenderer.class);
    bind(DateAttributeRenderer.class).to(DefaultDateAttributeRenderer.class);
    bind(DerivedNumericAttributeRenderer.class).to(DefaultDerivedNumberAttributeRenderer.class);
    bind(DerivedStringAttributeRenderer.class).to(DefaultDerivedStringAttributeRenderer.class);
    bind(FloatAttributeRenderer.class).to(DefaultFloatAttributeRenderer.class);
    bind(IntegerAttributeRenderer.class).to(DefaultIntegerAttributeRenderer.class);
    bind(SequenceAttributeRenderer.class).to(AgronDefaultSequenceAttributeRenderer.class); 
    bind(TextAreaAttributeRenderer.class).to(DefaultTextAreaAttributeRenderer.class);
    bind(TextAttributeRenderer.class).to(DefaultTextAttributeRenderer.class);
    bind(UOMAttributeRenderer.class).to(DefaultUOMAttributeRenderer.class);
    bind(URLAttributeRenderer.class).to(DefaultURLAttributeRenderer.class);
    bind(ImageAttributeRenderer.class).to(DefaultImageAttributeRenderer.class);
    bind(MultiEntryAttributeRenderer.class).to(DefaultMultiEntryAttributeRenderer.class);
    bind(ObjectReferenceAttributeRenderer.class).to(DefaultObjectReferenceAttributeRenderer.class);
    bind(MultiEntryOrderableAttributeRenderer.class).to(DefaultMultiEntryOrderableAttributeRenderer.class);
    if (RENDER_MODE == 0) {
      bind(CascadingAttributeRenderer.class).to(DefaultCascadingAttributeRenderer.class);
      bind(MultiSelectAttributeRenderer.class).to(DefaultMultiSelectAttributeRenderer.class);
      bind(SingleListAttributeRenderer.class).to(DefaultSingleListAttributeRenderer.class);
      bind(CompositeAttributeRenderer.class).to(DefaultCompositeAttributeRenderer.class);
      bind(UserListAttributeRenderer.class).to(DefaultUserListAttributeRenderer.class);
      bind(ObjectReferenceListAttributeRenderer.class).to(DefaultObjectReferenceListAttributeRenderer.class);
      bind(ColorChoiceAttributeRenderer.class).to(DefaultColorChoiceAttributeRenderer.class);
      bind(CareWashAttributeRenderer.class).to(DefaultCareWashAttributeRenderer.class);
    } else {
      bind(MultiSelectAttributeRenderer.class).to(Select2MultiSelectAttributeRenderer.class);
      bind(SingleListAttributeRenderer.class).to(Select2SingleListAttributeRenderer.class);
      bind(CompositeAttributeRenderer.class).to(Select2CompositeAttributeRenderer.class);
      bind(UserListAttributeRenderer.class).to(Select2UserListAttributeRenderer.class);
      bind(ObjectReferenceListAttributeRenderer.class).to(Select2ObjectReferenceListAttributeRenderer.class);
      bind(ColorChoiceAttributeRenderer.class).to(Select2ColorChoiceAttributeRenderer.class);
      bind(CareWashAttributeRenderer.class).to(Select2CareWashAttributeRenderer.class);
      bind(CascadingAttributeRenderer.class).to(Select2CascadingAttributeRenderer.class);
    } 
    bind(FormFieldRenderer.class).to(DefaultFormFieldRenderer.class);
    bind(ObjectThumbnailPlugin.class).to(DefaultObjectThumbnailPlugin.class);
    bind(IconButtonRenderer.class).to(DefaultIconButtonRenderer.class);
    bind(ButtonRenderer.class).to(DefaultButtonRenderer.class);
  }
}
