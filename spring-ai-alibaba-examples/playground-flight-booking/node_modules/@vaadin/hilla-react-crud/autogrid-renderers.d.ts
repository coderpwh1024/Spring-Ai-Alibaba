import type { GridItemModel } from '@vaadin/react-components/Grid.js';
import type { GridColumnElement } from '@vaadin/react-components/GridColumn.js';
import { type ComponentType, type JSX } from 'react';
import type { ItemCounts } from './data-provider';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
export type RendererOptions<TItem> = {
    item: TItem;
    model: GridItemModel<TItem>;
    original: GridColumnElement<TItem>;
};
export declare function AutoGridIntegerRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridDecimalRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridEnumRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridBooleanRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridDateRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridTimeRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridDateTimeRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridJsonRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element;
export declare function AutoGridRowNumberRenderer<TItem>({ model }: RendererOptions<TItem>): JSX.Element;
export type FooterContextType = {
    totalCount?: boolean;
    filteredCount?: boolean;
    footerCountRenderer?: ComponentType<ItemCounts>;
    itemCounts?: ItemCounts;
};
export declare const FooterContext: import("react").Context<FooterContextType>;
export declare function AutoGridFooterItemCountRenderer(): JSX.Element;
//# sourceMappingURL=autogrid-renderers.d.ts.map