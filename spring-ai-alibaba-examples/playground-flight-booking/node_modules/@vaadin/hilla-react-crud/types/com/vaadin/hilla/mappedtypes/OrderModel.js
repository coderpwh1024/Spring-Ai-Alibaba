import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotBlank as NotBlank_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import DirectionModel_1 from "../../../../org/springframework/data/domain/Sort/DirectionModel.js";
import NullHandlingModel_1 from "../../../../org/springframework/data/domain/Sort/NullHandlingModel.js";
class OrderModel extends ObjectModel_1 {
  static createEmptyValue = makeObjectEmptyValueCreator_1(OrderModel);
  get direction() {
    return this[_getPropertyModel_1]("direction", (parent, key) => new DirectionModel_1(parent, key, false));
  }
  get property() {
    return this[_getPropertyModel_1]("property", (parent, key) => new StringModel_1(parent, key, false, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
  }
  get ignoreCase() {
    return this[_getPropertyModel_1]("ignoreCase", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
  }
  get nullHandling() {
    return this[_getPropertyModel_1]("nullHandling", (parent, key) => new NullHandlingModel_1(parent, key, true));
  }
}
var OrderModel_default = OrderModel;
export {
  OrderModel_default as default
};
//# sourceMappingURL=OrderModel.js.map
