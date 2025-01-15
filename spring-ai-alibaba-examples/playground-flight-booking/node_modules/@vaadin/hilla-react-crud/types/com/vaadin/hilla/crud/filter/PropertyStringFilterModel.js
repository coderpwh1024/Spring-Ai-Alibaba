import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import FilterModel_1 from "./FilterModel.js";
import MatcherModel_1 from "./PropertyStringFilter/MatcherModel.js";
class PropertyStringFilterModel extends FilterModel_1 {
  static createEmptyValue = makeObjectEmptyValueCreator_1(PropertyStringFilterModel);
  get propertyId() {
    return this[_getPropertyModel_1]("propertyId", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
  }
  get filterValue() {
    return this[_getPropertyModel_1]("filterValue", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
  }
  get matcher() {
    return this[_getPropertyModel_1]("matcher", (parent, key) => new MatcherModel_1(parent, key, false));
  }
}
var PropertyStringFilterModel_default = PropertyStringFilterModel;
export {
  PropertyStringFilterModel_default as default
};
//# sourceMappingURL=PropertyStringFilterModel.js.map
