import {
  AutoGridBooleanRenderer,
  AutoGridDateRenderer,
  AutoGridDateTimeRenderer,
  AutoGridDecimalRenderer,
  AutoGridEnumRenderer,
  AutoGridIntegerRenderer,
  AutoGridJsonRenderer,
  AutoGridTimeRenderer
} from "./autogrid-renderers";
import {
  BooleanHeaderFilter,
  DateHeaderFilter,
  EnumHeaderFilter,
  NoHeaderFilter,
  NumberHeaderFilter,
  StringHeaderFilter,
  TimeHeaderFilter
} from "./header-filter";
function getTypeColumnOptions(propertyInfo, customColumnOptions) {
  switch (propertyInfo.type) {
    case "integer":
      return {
        autoWidth: true,
        textAlign: "end",
        flexGrow: 0,
        renderer: AutoGridIntegerRenderer,
        headerFilterRenderer: NumberHeaderFilter
      };
    case "decimal":
      return {
        autoWidth: true,
        textAlign: "end",
        flexGrow: 0,
        renderer: AutoGridDecimalRenderer,
        headerFilterRenderer: NumberHeaderFilter
      };
    case "boolean":
      return {
        autoWidth: true,
        textAlign: "end",
        flexGrow: 0,
        renderer: AutoGridBooleanRenderer,
        headerFilterRenderer: BooleanHeaderFilter
      };
    case "date":
      return {
        autoWidth: true,
        textAlign: "end",
        flexGrow: 0,
        renderer: AutoGridDateRenderer,
        headerFilterRenderer: DateHeaderFilter
      };
    case "time":
      return {
        autoWidth: true,
        textAlign: "end",
        flexGrow: 0,
        renderer: AutoGridTimeRenderer,
        headerFilterRenderer: TimeHeaderFilter
      };
    case "datetime":
      return {
        autoWidth: true,
        textAlign: "end",
        flexGrow: 0,
        renderer: AutoGridDateTimeRenderer,
        headerFilterRenderer: DateHeaderFilter
      };
    case "enum":
      return {
        autoWidth: true,
        renderer: AutoGridEnumRenderer,
        headerFilterRenderer: EnumHeaderFilter
      };
    case "string":
      return {
        autoWidth: true,
        headerFilterRenderer: StringHeaderFilter
      };
    case "object":
      return {
        autoWidth: true,
        renderer: customColumnOptions?.path !== void 0 && customColumnOptions.renderer == null ? null : AutoGridJsonRenderer,
        headerFilterRenderer: NoHeaderFilter
      };
    default:
      return {
        autoWidth: true,
        headerFilterRenderer: NoHeaderFilter
      };
  }
}
function getColumnOptions(propertyInfo, customColumnOptions) {
  const typeColumnOptions = getTypeColumnOptions(propertyInfo, customColumnOptions);
  const headerFilterRenderer = customColumnOptions?.filterable === false ? NoHeaderFilter : typeColumnOptions.headerFilterRenderer ?? NoHeaderFilter;
  return customColumnOptions ? { ...typeColumnOptions, headerFilterRenderer, ...customColumnOptions } : typeColumnOptions;
}
export {
  getColumnOptions
};
//# sourceMappingURL=autogrid-columns.js.map
