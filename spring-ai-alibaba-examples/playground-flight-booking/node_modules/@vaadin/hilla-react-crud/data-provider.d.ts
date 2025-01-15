import type { GridDataProviderCallback, GridDataProviderParams } from '@vaadin/react-components/Grid';
import type { GridDataProvider } from '@vaadin/react-components/Grid';
import type { CountService, ListService } from './crud';
import type FilterUnion from './types/com/vaadin/hilla/crud/filter/FilterUnion';
import type Sort from './types/com/vaadin/hilla/mappedtypes/Sort';
type MaybeCountService<TItem> = Partial<CountService<TItem>>;
type ListAndMaybeCountService<TItem> = ListService<TItem> & MaybeCountService<TItem>;
type ListAndCountService<TItem> = CountService<TItem> & ListService<TItem>;
type PageRequest = {
    pageNumber: number;
    pageSize: number;
    sort: Sort;
};
type DataPage<TItem> = {
    items: TItem[];
    pageRequest: PageRequest;
};
export type ItemCounts = {
    totalCount?: number;
    filteredCount?: number;
};
type AfterLoadCallback = (result: ItemCounts) => void;
type DataProviderOptions = {
    initialFilter?: FilterUnion;
    loadTotalCount?: boolean;
    afterLoad?: AfterLoadCallback;
};
export declare function isCountService<TItem>(service: ListAndMaybeCountService<TItem>): service is ListAndCountService<TItem>;
export declare abstract class DataProvider<TItem> {
    protected readonly service: ListAndMaybeCountService<TItem>;
    protected readonly loadTotalCount?: boolean;
    protected readonly afterLoadCallback?: AfterLoadCallback;
    protected filter: FilterUnion | undefined;
    protected totalCount: number | undefined;
    protected filteredCount: number | undefined;
    constructor(service: ListAndMaybeCountService<TItem>, options?: DataProviderOptions);
    reset(): void;
    setFilter(filter: FilterUnion | undefined): void;
    load(params: GridDataProviderParams<TItem>, callback: GridDataProviderCallback<TItem>): Promise<void>;
    protected fetchPage(params: GridDataProviderParams<TItem>): Promise<DataPage<TItem>>;
    protected abstract fetchTotalCount(page: DataPage<TItem>): Promise<number | undefined> | number | undefined;
    protected abstract fetchFilteredCount(page: DataPage<TItem>): Promise<number | undefined> | number | undefined;
}
export declare class InfiniteDataProvider<TItem> extends DataProvider<TItem> {
    protected fetchTotalCount(): undefined;
    protected fetchFilteredCount(page: DataPage<TItem>): number | undefined;
}
export declare class FixedSizeDataProvider<TItem> extends DataProvider<TItem> {
    service: ListAndCountService<TItem>;
    constructor(service: ListAndMaybeCountService<TItem>, options?: DataProviderOptions);
    protected fetchTotalCount(): Promise<number | undefined>;
    protected fetchFilteredCount(): Promise<number | undefined>;
}
export declare function createDataProvider<TItem>(service: ListAndMaybeCountService<TItem>, options?: DataProviderOptions): DataProvider<TItem>;
type UseDataProviderResult<TItem> = Readonly<{
    dataProvider: GridDataProvider<TItem>;
    refresh(): void;
}>;
export declare function useDataProvider<TItem>(service: ListAndMaybeCountService<TItem>, filter?: FilterUnion): UseDataProviderResult<TItem>;
export {};
//# sourceMappingURL=data-provider.d.ts.map