import { StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import FilterModel_1 from "./FilterModel.js";
import type PropertyStringFilter_1 from "./PropertyStringFilter.js";
import MatcherModel_1 from "./PropertyStringFilter/MatcherModel.js";
declare class PropertyStringFilterModel<T extends PropertyStringFilter_1 = PropertyStringFilter_1> extends FilterModel_1<T> {
    static createEmptyValue: () => PropertyStringFilter_1;
    get propertyId(): StringModel_1;
    get filterValue(): StringModel_1;
    get matcher(): MatcherModel_1;
}
export default PropertyStringFilterModel;
//# sourceMappingURL=PropertyStringFilterModel.d.ts.map