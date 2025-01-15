import type { GridColumnProps } from '@vaadin/react-components/GridColumn.js';
import { type ComponentType, type JSX, type ReactElement } from 'react';
import type FilterUnion from './types/com/vaadin/hilla/crud/filter/FilterUnion';
type ExtractComponentTypeProps<T extends ComponentType<any>> = T extends ComponentType<infer U> ? U : never;
export type HeaderRendererProps = ExtractComponentTypeProps<NonNullable<Required<GridColumnProps<unknown>>['headerRenderer']>>;
export type HeaderFilterRendererProps = HeaderRendererProps & {
    /**
     * Allows to set custom filters for the column.
     * This is used by the header filter components.
     * @param filter - The filter to set in the filter list.
     */
    setFilter(filter: FilterUnion): void;
};
export type HeaderFilterProps = Readonly<{
    /**
     * If true, the column can be sorted. This is useful to disable sorting for
     * properties that are not sortable in the backend, or that require excessive processing.
     */
    sortable?: boolean;
    /**
     * If true, the column can be filtered. This is useful to disable filtering for
     * properties that are not sortable in the backend, or that require excessive processing.
     */
    filterable?: boolean;
    /**
     * Placeholder text for the filter input.
     * Only applies to string, number and date/time value filters.
     */
    filterPlaceholder?: string;
    /**
     * Debounce time for the filter input in milliseconds.
     * Only applies to string value filters and number value filters.
     */
    filterDebounceTime?: number;
    /**
     * Minimum length for the filter input.
     * Only applies to string value filters.
     */
    filterMinLength?: number;
    /**
     * Custom renderer for the filter in the header.
     */
    headerFilterRenderer?: ComponentType<HeaderFilterRendererProps>;
}>;
export declare function StringHeaderFilter(): ReactElement;
export declare function NumberHeaderFilter(): ReactElement;
export declare function EnumHeaderFilter(): ReactElement;
export declare function BooleanHeaderFilter(): ReactElement;
export declare function DateHeaderFilter(): ReactElement;
export declare function TimeHeaderFilter(): ReactElement;
export declare function NoHeaderFilter(): ReactElement;
export declare function HeaderFilterWrapper({ original }: HeaderRendererProps): JSX.Element | null;
export {};
//# sourceMappingURL=header-filter.d.ts.map