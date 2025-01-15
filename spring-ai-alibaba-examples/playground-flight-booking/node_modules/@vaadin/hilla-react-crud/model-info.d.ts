import { type AbstractModel, type DetachedModelConstructor, type ModelMetadata } from '@vaadin/hilla-lit-form';
export type PropertyType = 'boolean' | 'date' | 'datetime' | 'decimal' | 'enum' | 'integer' | 'object' | 'string' | 'time' | undefined;
export interface PropertyInfo {
    name: string;
    humanReadableName: string;
    type: PropertyType;
    meta: ModelMetadata;
    model: AbstractModel;
}
export declare function hasAnnotation(meta: ModelMetadata, annotationName: string): boolean;
export declare function _generateHeader(path: string): string;
export declare class ModelInfo {
    private readonly modelInstance;
    readonly idProperty?: PropertyInfo;
    constructor(model: DetachedModelConstructor<AbstractModel>, idPropertyName?: string);
    private static resolveIdProperty;
    private static resolvePropertyModel;
    getRootProperties(path?: string): PropertyInfo[];
    getProperty(path: string): PropertyInfo | undefined;
    getProperties(paths: string[]): PropertyInfo[];
}
export declare function getDefaultProperties(modelInfo: ModelInfo): PropertyInfo[];
//# sourceMappingURL=model-info.d.ts.map