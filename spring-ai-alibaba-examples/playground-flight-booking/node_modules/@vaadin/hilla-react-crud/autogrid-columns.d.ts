import type { GridColumnProps } from '@vaadin/react-components/GridColumn.js';
import { type HeaderFilterProps } from './header-filter';
import type { PropertyInfo } from './model-info';
export type ColumnOptions = HeaderFilterProps & Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'>;
export declare function getColumnOptions(propertyInfo: PropertyInfo, customColumnOptions: ColumnOptions | undefined): ColumnOptions;
//# sourceMappingURL=autogrid-columns.d.ts.map