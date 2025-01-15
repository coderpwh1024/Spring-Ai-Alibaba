import { ArrayModel as ArrayModel_1 } from "@vaadin/hilla-lit-form";
import type AndFilter_1 from "./AndFilter.js";
import FilterModel_1 from "./FilterModel.js";
declare class AndFilterModel<T extends AndFilter_1 = AndFilter_1> extends FilterModel_1<T> {
    static createEmptyValue: () => AndFilter_1;
    get children(): ArrayModel_1<FilterModel_1>;
}
export default AndFilterModel;
//# sourceMappingURL=AndFilterModel.d.ts.map