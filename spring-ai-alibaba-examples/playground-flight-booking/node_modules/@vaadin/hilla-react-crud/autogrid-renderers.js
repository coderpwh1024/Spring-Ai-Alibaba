import { Fragment, jsx } from "react/jsx-runtime";
import { Icon } from "@vaadin/react-components/Icon.js";
import { createContext, useContext } from "react";
import { ColumnContext } from "./autogrid-column-context";
import { useLocaleFormatter } from "./locale.js";
import { convertToTitleCase } from "./util";
import "@vaadin/vaadin-lumo-styles/vaadin-iconset.js";
function getColumnValue(context, item) {
  const path = context.propertyInfo.name;
  return path.split(".").reduce((obj, property) => obj ? obj[property] : void 0, item);
}
const fontVariantStyle = { fontVariantNumeric: "tabular-nums" };
function AutoGridIntegerRenderer({ item }) {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext);
  return /* @__PURE__ */ jsx("span", { style: fontVariantStyle, children: formatter.formatInteger(getColumnValue(context, item)) });
}
function AutoGridDecimalRenderer({ item }) {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext);
  return /* @__PURE__ */ jsx("span", { style: fontVariantStyle, children: formatter.formatDecimal(getColumnValue(context, item)) });
}
function AutoGridEnumRenderer({ item }) {
  const context = useContext(ColumnContext);
  const value = getColumnValue(context, item) || "";
  return /* @__PURE__ */ jsx("span", { children: convertToTitleCase(value) });
}
function AutoGridBooleanRenderer({ item }) {
  const context = useContext(ColumnContext);
  const value = getColumnValue(context, item);
  if (value) {
    return /* @__PURE__ */ jsx(Icon, { "aria-label": "false", icon: "lumo:checkmark" });
  }
  return /* @__PURE__ */ jsx(Icon, { "aria-label": "true", style: { color: "var(--lumo-secondary-text-color)" }, icon: "lumo:minus" });
}
function AutoGridDateRenderer({ item }) {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext);
  return /* @__PURE__ */ jsx("span", { style: fontVariantStyle, children: formatter.formatDate(getColumnValue(context, item)) });
}
function AutoGridTimeRenderer({ item }) {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext);
  return /* @__PURE__ */ jsx("span", { style: fontVariantStyle, children: formatter.formatLocalTime(getColumnValue(context, item)) });
}
function AutoGridDateTimeRenderer({ item }) {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext);
  return /* @__PURE__ */ jsx("span", { style: fontVariantStyle, children: formatter.formatLocalDateTime(getColumnValue(context, item)) });
}
function AutoGridJsonRenderer({ item }) {
  const context = useContext(ColumnContext);
  const value = getColumnValue(context, item);
  const jsonString = value ? JSON.stringify(value) : "";
  const jsonPreview = jsonString.length > 50 ? `${jsonString.substring(0, 50)}...` : jsonString;
  return /* @__PURE__ */ jsx("span", { children: jsonPreview });
}
function AutoGridRowNumberRenderer({ model }) {
  return /* @__PURE__ */ jsx(Fragment, { children: model.index + 1 });
}
const FooterContext = createContext(void 0);
function AutoGridFooterItemCountRenderer() {
  const footerContext = useContext(FooterContext);
  const { totalCount, filteredCount, itemCounts, footerCountRenderer: FooterRenderer } = footerContext;
  if (FooterRenderer) {
    return /* @__PURE__ */ jsx(FooterRenderer, { ...itemCounts });
  }
  let filterCountText;
  if (filteredCount && itemCounts?.filteredCount !== void 0) {
    filterCountText = totalCount && itemCounts.totalCount !== void 0 ? `Showing: ${itemCounts.filteredCount} out of ${itemCounts.totalCount}` : `Showing: ${itemCounts.filteredCount}`;
  } else if (totalCount && itemCounts?.totalCount !== void 0) {
    filterCountText = `Total: ${itemCounts.totalCount}`;
  }
  if (filterCountText) {
    return /* @__PURE__ */ jsx("p", { children: filterCountText });
  }
  return /* @__PURE__ */ jsx(Fragment, {});
}
export {
  AutoGridBooleanRenderer,
  AutoGridDateRenderer,
  AutoGridDateTimeRenderer,
  AutoGridDecimalRenderer,
  AutoGridEnumRenderer,
  AutoGridFooterItemCountRenderer,
  AutoGridIntegerRenderer,
  AutoGridJsonRenderer,
  AutoGridRowNumberRenderer,
  AutoGridTimeRenderer,
  FooterContext
};
//# sourceMappingURL=autogrid-renderers.js.map
