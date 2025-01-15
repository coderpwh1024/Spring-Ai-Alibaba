import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import FilterModel_1 from "./FilterModel.js";
class AndFilterModel extends FilterModel_1 {
  static createEmptyValue = makeObjectEmptyValueCreator_1(AndFilterModel);
  get children() {
    return this[_getPropertyModel_1]("children", (parent, key) => new ArrayModel_1(parent, key, false, (parent2, key2) => new FilterModel_1(parent2, key2, false), { meta: { javaType: "java.util.List" } }));
  }
}
var AndFilterModel_default = AndFilterModel;
export {
  AndFilterModel_default as default
};
//# sourceMappingURL=AndFilterModel.js.map
