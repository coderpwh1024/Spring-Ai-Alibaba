import { type CSSProperties } from 'react';
import type FilterUnion from './types/com/vaadin/hilla/crud/filter/FilterUnion';
export type ComponentStyleProps = Readonly<{
    id?: string;
    style?: CSSProperties;
    className?: string;
}>;
export declare function convertToTitleCase(inputString: string): string;
export declare function registerStylesheet(stylesheet: CSSStyleSheet): void;
export declare function featureRegistration<C extends (...args: any[]) => any>(Component: C, feature: string): C;
export declare function isFilterEmpty(filter: FilterUnion): boolean;
//# sourceMappingURL=util.d.ts.map