import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import OrderModel_1 from "./OrderModel.js";
class SortModel extends ObjectModel_1 {
  static createEmptyValue = makeObjectEmptyValueCreator_1(SortModel);
  get orders() {
    return this[_getPropertyModel_1]("orders", (parent, key) => new ArrayModel_1(parent, key, false, (parent2, key2) => new OrderModel_1(parent2, key2, true), { meta: { javaType: "java.util.List" } }));
  }
}
var SortModel_default = SortModel;
export {
  SortModel_default as default
};
//# sourceMappingURL=SortModel.js.map
