import type { GridSorterDirection } from '@vaadin/react-components/GridSorter.js';
import { type Dispatch, type SetStateAction, type ComponentType } from 'react';
import type { ColumnOptions } from './autogrid-columns';
import type { HeaderFilterRendererProps } from './header-filter';
import type { PropertyInfo } from './model-info';
import type FilterUnion from './types/com/vaadin/hilla/crud/filter/FilterUnion';
export interface SorterState {
    direction: GridSorterDirection;
}
export type SortState = Record<string, SorterState | undefined>;
export type ColumnContext = Readonly<{
    propertyInfo: PropertyInfo;
    setColumnFilter(filter: FilterUnion, filterKey: string): void;
    sortState: SortState;
    setSortState: Dispatch<SetStateAction<SortState>>;
    customColumnOptions?: ColumnOptions;
    headerFilterRenderer: ComponentType<HeaderFilterRendererProps>;
    filterKey: string;
}>;
export declare const ColumnContext: import("react").Context<Readonly<{
    propertyInfo: PropertyInfo;
    setColumnFilter(filter: FilterUnion, filterKey: string): void;
    sortState: SortState;
    setSortState: Dispatch<SetStateAction<SortState>>;
    customColumnOptions?: ColumnOptions | undefined;
    headerFilterRenderer: ComponentType<HeaderFilterRendererProps>;
    filterKey: string;
}> | null>;
export type CustomColumnContext = Readonly<{
    setColumnFilter(filter: FilterUnion, filterKey: string): void;
    headerFilterRenderer: ComponentType<HeaderFilterRendererProps>;
    filterKey: string;
}>;
export declare const CustomColumnContext: import("react").Context<Readonly<{
    setColumnFilter(filter: FilterUnion, filterKey: string): void;
    headerFilterRenderer: ComponentType<HeaderFilterRendererProps>;
    filterKey: string;
}> | null>;
//# sourceMappingURL=autogrid-column-context.d.ts.map