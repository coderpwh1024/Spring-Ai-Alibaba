import { ArrayModel as ArrayModel_1 } from "@vaadin/hilla-lit-form";
import FilterModel_1 from "./FilterModel.js";
import type OrFilter_1 from "./OrFilter.js";
declare class OrFilterModel<T extends OrFilter_1 = OrFilter_1> extends FilterModel_1<T> {
    static createEmptyValue: () => OrFilter_1;
    get children(): ArrayModel_1<FilterModel_1>;
}
export default OrFilterModel;
//# sourceMappingURL=OrFilterModel.d.ts.map