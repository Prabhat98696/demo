package com.lcs.wc.client.web.html.header;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.lcs.wc.client.web.PageName;
import com.lcs.wc.client.web.html.ButtonRenderer;
import com.lcs.wc.client.web.html.DefaultButtonRenderer;
import com.lcs.wc.client.web.html.DefaultIconButtonRenderer;
import com.lcs.wc.client.web.html.IconButtonRenderer;  
import com.lcs.wc.client.web.html.action.ActionMenuProvider;
import com.lcs.wc.client.web.html.header.action.CommonActionsProvider;
import com.lcs.wc.client.web.html.header.action.DefaultCommonActionsProvider;
import com.lcs.wc.client.web.html.header.action.DiscussionForumAction;
import com.lcs.wc.client.web.html.header.action.DiscussionForumActionImpl;
import com.lcs.wc.client.web.html.header.action.EmailAction;
import com.lcs.wc.client.web.html.header.action.EmailActionImpl;
import com.lcs.wc.client.web.html.header.action.LifecycleManagedAction;
import com.lcs.wc.client.web.html.header.action.LifecycleManagedActionImpl;
import com.lcs.wc.client.web.html.header.action.SubscriptionAction;
import com.lcs.wc.client.web.html.header.action.SubscriptionActionImpl;
import com.lcs.wc.client.web.html.header.action.TWXDashboardAction;
import com.lcs.wc.client.web.html.header.action.TWXDashboardActionImpl;
import com.lcs.wc.client.web.html.header.action.TeamManagedAction;
import com.lcs.wc.client.web.html.header.action.TeamManagedActionImpl;
import com.lcs.wc.client.web.html.header.action.WhereUsedAction;
import com.lcs.wc.client.web.html.header.action.WhereUsedActionImpl;
import com.lcs.wc.client.web.html.header.context.ContextBarRenderer;
import com.lcs.wc.client.web.html.header.menu.DefaultMenuRenderer;
import com.lcs.wc.client.web.html.header.menu.MenuBarRenderer;
import com.lcs.wc.client.web.html.header.menu.MenuRenderer;
import com.lcs.wc.client.web.html.header.title.CreateImagePageTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.CreateSpecPageTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.DefaultUpdatePageTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.EditColorwaySourcingTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.EditConstructionSetTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.EditMeasurementsTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.EditSourceToSeasonTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.EditSourcingConfigurationTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.MassActivateDeactivateSkuSizeTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.ProductSeasonTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.ReusableTableTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.TitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.UpdateCostSheetTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.UpdateImagePageTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.UpdateMultipleSKUSizeTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.UpdateSpecPageTitleBarRenderer;
import com.lcs.wc.client.web.html.header.title.ViewAllVariationsTitleBarRenderer;
import com.lcs.wc.construction.html.EditConstructionActionMenuProvider;
import com.lcs.wc.flexbom.html.BOMActionMenuProvider;
import com.lcs.wc.flexbom.html.MaterialBOMActionMenuProvider;
import com.lcs.wc.material.html.CreateMaterialColorTitleBarRenderer;
import com.lcs.wc.material.html.MaterialActionMenuProvider;
import com.lcs.wc.material.html.MaterialColorActionMenuProvider;
import com.lcs.wc.material.html.MaterialContextBarRenderer;
import com.lcs.wc.material.html.MaterialMenuBarRenderer;
import com.lcs.wc.material.html.ViewMaterialColorTitleBarRenderer;
import com.lcs.wc.material.html.ViewMaterialTitleBarRenderer;
import com.lcs.wc.product.html.AgronProductContextBarRenderer;
import com.lcs.wc.product.html.JqueryIconButtonActionMenuRenderer;
import com.lcs.wc.product.html.LibraryImagePageTitleBarRenderer;
import com.lcs.wc.product.html.ProductContextBarRenderer;
import com.lcs.wc.product.html.ProductMenuBarRenderer;
import com.lcs.wc.product.html.ViewProductTitleBarRenderer;
import com.lcs.wc.season.html.ColorwaySizingMenuProvider;
import com.lcs.wc.season.html.LibraryImagePageActionMenuProvider;
import com.lcs.wc.season.html.SeasonProductHeaderActionMenuProvider;
import com.lcs.wc.season.html.SizingMenuProvider;
import com.lcs.wc.skusize.html.ViewColorwaySizeTitleBarRenderer;
import com.lcs.wc.sourcing.html.SourcingConfigActionMenuProvider;
import com.lcs.wc.sourcing.html.ViewCostSheetActionMenuProvider;
import java.lang.annotation.Annotation;

public class AgronPageHeaderRendererModule extends AbstractModule {
  protected void configure() {
    bind(ButtonRenderer.class).to(DefaultButtonRenderer.class);
    bind(IconButtonRenderer.class).to(DefaultIconButtonRenderer.class);
    bind(IconButtonRenderer.class).annotatedWith((Annotation)Names.named("jquery-actions-menu-dropdown"))
      .to(JqueryIconButtonActionMenuRenderer.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("product-actions"))
      .to(SeasonProductHeaderActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("sizing-actions")).to(SizingMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("colorway-sizing-actions"))
      .to(ColorwaySizingMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("bom-actions")).to(BOMActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("material-bom-actions"))
      .to(MaterialBOMActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("sourcingConfig-actions"))
      .to(SourcingConfigActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("edit-construction-actions"))
      .to(EditConstructionActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("view-cost-sheet-details-actions"))
      .to(ViewCostSheetActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("images-actions"))
      .to(LibraryImagePageActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("material-actions"))
      .to(MaterialActionMenuProvider.class);
    bind(ActionMenuProvider.class).annotatedWith((Annotation)Names.named("material-color-actions"))
      .to(MaterialColorActionMenuProvider.class);
    bind(PageHeaderRenderer.class).to(DefaultPageHeaderRenderer.class); 
    
    MapBinder<String, MenuBarRenderer> menuBarRendererMap = MapBinder.newMapBinder(binder(), String.class, MenuBarRenderer.class);
    menuBarRendererMap.addBinding("PRODUCT_PAGE_HEADER").to(ProductMenuBarRenderer.class);
    menuBarRendererMap.addBinding("MATERIAL_PAGE_HEADER").to(MaterialMenuBarRenderer.class);
    MapBinder<String, TitleBarRenderer> titleBarRendererMap = MapBinder.newMapBinder(binder(), String.class, TitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_MATERIAL_PAGE.name()).to(ViewMaterialTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_MATERIAL_PAGE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_MATERIAL_PAGE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_MATERIAL_SUPPLIER_PAGE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_MATERIAL_PAGE_PRICING.name())
      .to(ViewMaterialTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_SP_PAGE.name()).to(ViewProductTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_PRODUCT_PAGE.name()).to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SP_PAGE.name()).to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_PRODUCT_PAGE.name()).to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_PRODUCT_SEASON_PAGE.name())
      .to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_SKU_PAGE.name()).to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_SKU_SEASON_PAGE.name()).to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_SP_PAGE.name()).to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_SEASON_SKU_PAGE.name()).to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SEASON_SKU_PAGE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SKU_PAGE.name()).to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_IMAGEPAGE_DOCUMENT_PAGE.name())
      .to(CreateImagePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SINGLE_SKUSIZE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_MULTIPLE_SKUSIZE.name())
      .to(UpdateMultipleSKUSizeTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_IMAGEPAGE_DOCUMENT_PAGE.name())
      .to(LibraryImagePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_3D_PAGE_DOCUMENT_PAGE.name())
      .to(LibraryImagePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_IMAGEPAGE_DOCUMENT_PAGE.name())
      .to(UpdateImagePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_COLORWAY_IMAGEPAGE_DOCUMENT_PAGE.name())
      .to(CreateImagePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_COLORWAY_IMAGEPAGE_DOCUMENT_PAGE.name())
      .to(UpdateImagePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.MASS_ACTIVATE_SKUSIZE.name())
      .to(MassActivateDeactivateSkuSizeTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.MASS_ACTIVATE_SKUSIZE_SEASON.name())
      .to(MassActivateDeactivateSkuSizeTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.MASS_ACTIVATE_SKUSIZE_SOURCE.name())
      .to(MassActivateDeactivateSkuSizeTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_SINGLE_SKUSIZE.name()).to(ViewColorwaySizeTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CONSTRUCTION_EDITOR2.name())
      .to(EditConstructionSetTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.EDIT_MEASUREMENTS.name()).to(EditMeasurementsTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.SET_IMAGE_PAGE.name()).to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.BOM_OVERRIDES_REPORT.name())
      .to(ViewAllVariationsTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_FLEXSPEC_PAGE.name()).to(CreateSpecPageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SOURCE_TO_SEASON_PAGE.name())
      .to(EditSourceToSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SOURCINGCONFIG_PAGE.name())
      .to(EditSourcingConfigurationTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_SOURCINGCONFIG_PAGE.name())
      .to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_SKU_SOURCING_PAGE.name())
      .to(EditColorwaySourcingTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_COSTING_PAGE.name()).to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_FLEXSPEC_PAGE.name()).to(UpdateSpecPageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.EDIT_MOACOLLECTION.name()).to(ReusableTableTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_MULTI_COSTSHEET_PAGE.name())
      .to(UpdateCostSheetTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_COSTSHEET_PAGE.name()).to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.WHATIF_MULTI_COSTSHEET_NEW_PAGE.name())
      .to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.WHATIF_MULTI_COSTSHEET_PAGE.name())
      .to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.COPY_COSTSHEET_PAGE.name()).to(ProductSeasonTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_MATERIALCOLOR_PAGE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.UPDATE_MATERIALCOLOR_PAGE.name())
      .to(DefaultUpdatePageTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.CREATE_MATERIALCOLOR_PAGE.name())
      .to(CreateMaterialColorTitleBarRenderer.class);
    titleBarRendererMap.addBinding(PageName.VIEW_MATERIALCOLOR_PAGE.name())
      .to(ViewMaterialColorTitleBarRenderer.class);
    MapBinder<String, ContextBarRenderer> contextBarRendererMap = MapBinder.newMapBinder(binder(), String.class, ContextBarRenderer.class);
    contextBarRendererMap.addBinding(PageName.VIEW_SP_PAGE.name()).to(AgronProductContextBarRenderer.class); 
    contextBarRendererMap.addBinding("VIEW_MATERIAL_PAGE").to(MaterialContextBarRenderer.class);
    bind(MenuRenderer.class).to(DefaultMenuRenderer.class);
    bind(CommonActionsProvider.class).to(DefaultCommonActionsProvider.class);
    bind(DiscussionForumAction.class).to(DiscussionForumActionImpl.class);
    bind(SubscriptionAction.class).to(SubscriptionActionImpl.class);
    bind(LifecycleManagedAction.class).to(LifecycleManagedActionImpl.class);
    bind(TeamManagedAction.class).to(TeamManagedActionImpl.class);
    bind(WhereUsedAction.class).to(WhereUsedActionImpl.class);
    bind(EmailAction.class).to(EmailActionImpl.class);
    bind(TWXDashboardAction.class).to(TWXDashboardActionImpl.class);
  }
}
