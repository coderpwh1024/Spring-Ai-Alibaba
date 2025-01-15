import { useMemo, useState } from "react";
import Direction from "./types/org/springframework/data/domain/Sort/Direction";
function createSort(params) {
  return {
    orders: params.sortOrders.filter((order) => order.direction != null).map((order) => ({
      property: order.path,
      direction: order.direction === "asc" ? Direction.ASC : Direction.DESC,
      ignoreCase: false
    }))
  };
}
function isCountService(service) {
  return !!service.count;
}
class DataProvider {
  service;
  loadTotalCount;
  afterLoadCallback;
  filter;
  totalCount;
  filteredCount;
  constructor(service, options = {}) {
    this.service = service;
    this.filter = options.initialFilter;
    this.loadTotalCount = options.loadTotalCount;
    this.afterLoadCallback = options.afterLoad;
    this.load = this.load.bind(this);
  }
  reset() {
    this.totalCount = void 0;
    this.filteredCount = void 0;
  }
  setFilter(filter) {
    this.reset();
    this.filter = filter;
  }
  async load(params, callback) {
    const page = await this.fetchPage(params);
    this.filteredCount = await this.fetchFilteredCount(page);
    if (this.loadTotalCount) {
      this.totalCount = await this.fetchTotalCount(page);
    }
    callback(page.items, this.filteredCount);
    if (this.afterLoadCallback) {
      this.afterLoadCallback({
        totalCount: this.totalCount,
        filteredCount: this.filteredCount
      });
    }
  }
  async fetchPage(params) {
    const sort = createSort(params);
    const pageNumber = params.page;
    const { pageSize } = params;
    const pageRequest = {
      pageNumber,
      pageSize,
      sort
    };
    const items = await this.service.list(pageRequest, this.filter);
    return { items, pageRequest };
  }
}
class InfiniteDataProvider extends DataProvider {
  // cannot be static, otherwise it does not implement superclass
  // eslint-disable-next-line @typescript-eslint/class-methods-use-this
  fetchTotalCount() {
    return void 0;
  }
  fetchFilteredCount(page) {
    const { items, pageRequest } = page;
    const { pageNumber, pageSize } = pageRequest;
    let infiniteScrollingSize;
    if (items.length === pageSize) {
      infiniteScrollingSize = (pageNumber + 1) * pageSize + 1;
      if (this.filteredCount !== void 0 && infiniteScrollingSize < this.filteredCount) {
        infiniteScrollingSize = this.filteredCount;
      }
    } else {
      infiniteScrollingSize = pageNumber * pageSize + items.length;
    }
    return infiniteScrollingSize;
  }
}
class FixedSizeDataProvider extends DataProvider {
  constructor(service, options = {}) {
    if (!isCountService(service)) {
      throw new Error("The provided service does not implement the CountService interface.");
    }
    super(service, options);
  }
  async fetchTotalCount() {
    if (this.totalCount !== void 0) {
      return this.totalCount;
    }
    return this.service.count(void 0);
  }
  async fetchFilteredCount() {
    if (this.filteredCount !== void 0) {
      return this.filteredCount;
    }
    return this.service.count(this.filter);
  }
}
function createDataProvider(service, options = {}) {
  if (isCountService(service)) {
    return new FixedSizeDataProvider(service, options);
  }
  return new InfiniteDataProvider(service, options);
}
function useDataProvider(service, filter) {
  const [refreshCounter, setRefreshCounter] = useState(0);
  const dataProvider = useMemo(() => createDataProvider(service, { initialFilter: filter }), [service]);
  dataProvider.setFilter(filter);
  const dataProviderFn = useMemo(() => dataProvider.load.bind(dataProvider), [dataProvider, filter, refreshCounter]);
  return {
    // eslint-disable-next-line @typescript-eslint/no-misused-promises
    dataProvider: dataProviderFn,
    refresh: () => {
      dataProvider.reset();
      setRefreshCounter(refreshCounter + 1);
    }
  };
}
export {
  DataProvider,
  FixedSizeDataProvider,
  InfiniteDataProvider,
  createDataProvider,
  isCountService,
  useDataProvider
};
//# sourceMappingURL=data-provider.js.map
