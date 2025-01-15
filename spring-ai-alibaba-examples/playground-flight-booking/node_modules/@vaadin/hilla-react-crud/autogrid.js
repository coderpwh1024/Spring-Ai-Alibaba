import { jsx } from "react/jsx-runtime";
import { Grid } from "@vaadin/react-components/Grid.js";
import { GridColumn } from "@vaadin/react-components/GridColumn.js";
import { GridColumnGroup } from "@vaadin/react-components/GridColumnGroup.js";
import {
  cloneElement,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useMemo,
  useRef,
  useState
} from "react";
import { ColumnContext, CustomColumnContext } from "./autogrid-column-context.js";
import { getColumnOptions } from "./autogrid-columns.js";
import { AutoGridFooterItemCountRenderer, AutoGridRowNumberRenderer, FooterContext } from "./autogrid-renderers.js";
import css from "./autogrid.obj.js";
import { createDataProvider, isCountService } from "./data-provider.js";
import { NoHeaderFilter, HeaderFilterWrapper } from "./header-filter";
import { HeaderSorter } from "./header-sorter";
import { getDefaultProperties, ModelInfo } from "./model-info.js";
import { isFilterEmpty, registerStylesheet } from "./util";
registerStylesheet(css);
function wrapCustomColumn(column, setColumnFilter, options) {
  const key = column.key ?? "no-key";
  const { header, headerRenderer } = column.props;
  const customOptions = options.columnOptions?.[key];
  const { header: customHeader, headerRenderer: customHeaderRenderer, headerFilterRenderer } = customOptions ?? {};
  const columnWithoutHeader = cloneElement(column, {
    header: null,
    headerRenderer: HeaderFilterWrapper
  });
  return /* @__PURE__ */ jsx(
    CustomColumnContext.Provider,
    {
      value: {
        setColumnFilter,
        headerFilterRenderer: headerFilterRenderer ?? NoHeaderFilter,
        filterKey: key
      },
      children: /* @__PURE__ */ jsx(
        GridColumnGroup,
        {
          header: customHeader ?? header,
          headerRenderer: customHeaderRenderer ?? headerRenderer,
          children: columnWithoutHeader
        },
        key
      )
    },
    key
  );
}
function addCustomColumns(columns, options, setColumnFilter) {
  if (!options.customColumns) {
    return columns;
  }
  const customColumns = options.noHeaderFilters ? options.customColumns : options.customColumns.map((column) => wrapCustomColumn(column, setColumnFilter, options));
  if (options.visibleColumns) {
    const columnMap = [...columns, ...customColumns].reduce((map, column) => {
      const { key } = column;
      if (key) {
        map.set(key, column);
      }
      return map;
    }, /* @__PURE__ */ new Map());
    return options.visibleColumns.map((path) => columnMap.get(path)).filter(Boolean);
  }
  return [...columns, ...customColumns];
}
function useColumns(properties, setColumnFilter, options) {
  const sortableProperties = properties.filter(
    (propertyInfo) => options.columnOptions?.[propertyInfo.name]?.sortable !== false
  );
  const [sortState, setSortState] = useState(
    sortableProperties.length > 0 ? { [sortableProperties[0].name]: { direction: "asc" } } : {}
  );
  let columns = properties.map((propertyInfo) => {
    let column;
    const customColumnOptions = options.columnOptions?.[propertyInfo.name];
    const { headerFilterRenderer, ...columnProps } = getColumnOptions(propertyInfo, customColumnOptions);
    if (!options.noHeaderFilters) {
      column = /* @__PURE__ */ jsx(GridColumnGroup, { headerRenderer: HeaderSorter, children: /* @__PURE__ */ jsx(GridColumn, { path: propertyInfo.name, headerRenderer: HeaderFilterWrapper, ...columnProps }) });
    } else {
      column = /* @__PURE__ */ jsx(GridColumn, { path: propertyInfo.name, headerRenderer: HeaderSorter, ...columnProps });
    }
    return /* @__PURE__ */ jsx(
      ColumnContext.Provider,
      {
        value: {
          propertyInfo,
          setColumnFilter,
          sortState,
          setSortState,
          customColumnOptions,
          headerFilterRenderer: headerFilterRenderer ?? NoHeaderFilter,
          filterKey: propertyInfo.name
        },
        children: column
      },
      propertyInfo.name
    );
  });
  columns = addCustomColumns(columns, options, setColumnFilter);
  if (options.hiddenColumns) {
    columns = columns.filter(({ key }) => !(key && options.hiddenColumns?.includes(key)));
  }
  if (options.rowNumbers) {
    columns = [
      /* @__PURE__ */ jsx(GridColumn, { width: "4em", flexGrow: 0, renderer: AutoGridRowNumberRenderer }, "rownumbers"),
      ...columns
    ];
  }
  const { totalCount, filteredCount, itemCounts, footerCountRenderer } = options;
  if (totalCount ?? filteredCount) {
    const col = /* @__PURE__ */ jsx(
      FooterContext.Provider,
      {
        value: {
          totalCount,
          filteredCount,
          footerCountRenderer,
          itemCounts
        },
        children: /* @__PURE__ */ jsx(GridColumnGroup, { footerRenderer: AutoGridFooterItemCountRenderer, children: columns })
      },
      "grid-footer"
    );
    columns = [col];
  }
  return columns;
}
function AutoGridInner({
  service,
  model,
  itemIdProperty,
  experimentalFilter,
  visibleColumns,
  hiddenColumns,
  noHeaderFilters,
  customColumns,
  columnOptions,
  rowNumbers,
  totalCount,
  filteredCount,
  footerCountRenderer,
  ...gridProps
}, ref) {
  const [internalFilter, setInternalFilter] = useState({ "@type": "and", children: [] });
  const [itemCounts, setItemCounts] = useState();
  const gridRef = useRef(null);
  const dataProviderRef = useRef();
  useImperativeHandle(
    ref,
    () => ({
      get grid() {
        return gridRef.current;
      },
      refresh() {
        dataProviderRef.current?.reset();
        gridRef.current?.clearCache();
      }
    }),
    []
  );
  const setHeaderFilter = (filter, filterKey) => {
    let changed = false;
    filter.key = filterKey;
    const indexOfFilter = filterKey ? internalFilter.children.findIndex((f) => f.key === filterKey) : -1;
    const isEmptyFilter = isFilterEmpty(filter);
    if (indexOfFilter >= 0 && isEmptyFilter) {
      internalFilter.children.splice(indexOfFilter, 1);
      changed = true;
    } else if (!isEmptyFilter) {
      if (indexOfFilter >= 0) {
        internalFilter.children[indexOfFilter] = filter;
        changed = true;
      } else {
        internalFilter.children.push(filter);
        changed = true;
      }
    }
    if (changed) {
      setInternalFilter({ ...internalFilter });
    }
  };
  const modelInfo = useMemo(() => new ModelInfo(model, itemIdProperty), [model]);
  const properties = visibleColumns ? modelInfo.getProperties(visibleColumns) : getDefaultProperties(modelInfo);
  const children = useColumns(properties, setHeaderFilter, {
    visibleColumns,
    hiddenColumns,
    noHeaderFilters,
    customColumns,
    columnOptions,
    rowNumbers,
    totalCount,
    filteredCount,
    footerCountRenderer,
    itemCounts
  });
  useEffect(() => {
    if (noHeaderFilters) {
      setInternalFilter({ "@type": "and", children: [] });
    }
  }, [noHeaderFilters]);
  useEffect(() => {
    if ((!isCountService(service) && totalCount) ?? filteredCount) {
      console.error(
        '"totalCount" and "filteredCount" props require the provided service to implement the CountService interface.'
      );
    }
    const grid = gridRef.current;
    const timeoutId = setTimeout(() => {
      let firstUpdate = true;
      const dataProvider = createDataProvider(service, {
        initialFilter: experimentalFilter ?? internalFilter,
        loadTotalCount: totalCount,
        afterLoad(newItemCounts) {
          setItemCounts(newItemCounts);
          if (firstUpdate) {
            firstUpdate = false;
            setTimeout(() => grid.recalculateColumnWidths(), 0);
          }
        }
      });
      dataProviderRef.current = dataProvider;
      gridRef.current.dataProvider = dataProvider.load.bind(dataProvider);
    }, 1);
    return () => clearTimeout(timeoutId);
  }, [model, service]);
  useEffect(() => {
    const dataProvider = dataProviderRef.current;
    const grid = gridRef.current;
    if (grid && dataProvider) {
      dataProvider.setFilter(experimentalFilter ?? internalFilter);
      grid.clearCache();
    }
  }, [experimentalFilter, internalFilter]);
  return /* @__PURE__ */ jsx(Grid, { itemIdPath: modelInfo.idProperty?.name, ...gridProps, ref: gridRef, children });
}
const AutoGrid = forwardRef(AutoGridInner);
export {
  AutoGrid
};
//# sourceMappingURL=autogrid.js.map
