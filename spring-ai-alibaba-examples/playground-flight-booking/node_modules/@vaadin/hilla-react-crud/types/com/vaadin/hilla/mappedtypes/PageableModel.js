import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import SortModel_1 from "./SortModel.js";
class PageableModel extends ObjectModel_1 {
  static createEmptyValue = makeObjectEmptyValueCreator_1(PageableModel);
  get pageNumber() {
    return this[_getPropertyModel_1]("pageNumber", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
  }
  get pageSize() {
    return this[_getPropertyModel_1]("pageSize", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
  }
  get sort() {
    return this[_getPropertyModel_1]("sort", (parent, key) => new SortModel_1(parent, key, false));
  }
}
var PageableModel_default = PageableModel;
export {
  PageableModel_default as default
};
//# sourceMappingURL=PageableModel.js.map
