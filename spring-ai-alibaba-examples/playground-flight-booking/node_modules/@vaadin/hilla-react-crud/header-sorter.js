import { Fragment, jsx } from "react/jsx-runtime";
import { GridSorter } from "@vaadin/react-components/GridSorter.js";
import { useContext } from "react";
import { ColumnContext } from "./autogrid-column-context.js";
function HeaderSorter() {
  const context = useContext(ColumnContext);
  const sorterState = context.sortState[context.propertyInfo.name];
  const direction = sorterState?.direction ?? null;
  const headerLabel = context.customColumnOptions?.header ?? context.propertyInfo.humanReadableName;
  return context.customColumnOptions?.sortable === false ? /* @__PURE__ */ jsx(Fragment, { children: headerLabel }) : /* @__PURE__ */ jsx(
    GridSorter,
    {
      path: context.propertyInfo.name,
      direction,
      onDirectionChanged: (e) => {
        context.setSortState((prevState) => {
          const newSorterState = e.detail.value ? { direction: e.detail.value } : void 0;
          return { ...prevState, [context.propertyInfo.name]: newSorterState };
        });
      },
      children: headerLabel
    }
  );
}
export {
  HeaderSorter
};
//# sourceMappingURL=header-sorter.js.map
