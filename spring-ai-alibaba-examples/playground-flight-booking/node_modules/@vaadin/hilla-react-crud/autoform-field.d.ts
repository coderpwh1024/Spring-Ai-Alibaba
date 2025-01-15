import { type Validator } from '@vaadin/hilla-lit-form';
import type { FieldDirectiveResult, UseFormResult } from '@vaadin/hilla-react-form';
import { type CSSProperties, type JSX } from 'react';
import type { PropertyInfo } from './model-info.js';
export type AutoFormFieldProps = Readonly<{
    propertyInfo: PropertyInfo;
    form: UseFormResult<any>;
    options: FieldOptions;
    disabled?: boolean;
}>;
type CustomFormFieldProps = FieldDirectiveResult & Readonly<{
    label?: string;
    disabled?: boolean;
}>;
export type FieldOptions = Readonly<{
    /**
     * The id to apply to the field.
     */
    id?: string;
    /**
     * The class names to add to the field.
     */
    className?: string;
    /**
     * The style to apply to the field.
     */
    style?: CSSProperties;
    /**
     * The label to show for the field. If not specified, a human-readable label
     * is generated from the property name.
     */
    label?: string;
    /**
     * The placeholder to when the field is empty.
     *
     * Note that some field types, such as checkbox, do not support a placeholder.
     */
    placeholder?: string;
    /**
     * The helper text to display below the field.
     *
     * Note that some field types, such as checkbox, do not support a helper text.
     */
    helperText?: string;
    /**
     * The number of columns to span. This value is passed to the underlying
     * FormLayout, unless a custom layout is used. In that case, the value is
     * ignored.
     */
    colspan?: number;
    /**
     * Whether the field should be disabled.
     */
    disabled?: boolean;
    /**
     * Whether the field should be readonly.
     */
    readonly?: boolean;
    /**
     * The element to render for the field. This allows customizing field props
     * that are not supported by the field options, or to render a different field
     * component. Other field options are automatically applied to the element,
     * and the element is automatically bound to the form. If not specified, a
     * default field element is rendered based on the property type.
     *
     * The element must be a field component, such as TextField, TextArea,
     * NumberField, etc., otherwise form binding will not work. For more
     * sophisticated customizations, use the `renderer` option.
     *
     * If the field options also specify a renderer function, then the element is
     * ignored.
     *
     * Example enabling the clear button for a text field:
     * ```tsx
     * {
     *   element: <TextField clearButtonVisible />
     * }
     * ```
     *
     * Example rendering a text area instead of a text field:
     * ```tsx
     * {
     *   element: <TextArea />
     * }
     * ```
     */
    element?: JSX.Element;
    /**
     * Allows to specify a custom renderer for the field, for example to render a
     * custom type of field or apply an additional layout around the field. The
     * renderer receives field props that must be applied to the custom field
     * component in order to connect it to the form.
     *
     * In order to customize one of the default fields, or render a different type
     * of field, consider using the `element` option instead.
     *
     * Example:
     * ```tsx
     * {
     *   renderer: ({ field }) => (
     *     <div>
     *       <TextArea {...field} />
     *       <p>Number of words: {calculateNumberOfWords()}</p>
     *     </div>
     *   )
     * }
     * ```
     */
    renderer?(props: {
        field: CustomFormFieldProps;
    }): JSX.Element;
    /**
     * Validators to apply to the field. The validators are added to the form
     * when the field is rendered.
     * UseMemo is recommended for the validators, so that they are not recreated
     * on every render.
     */
    validators?: Validator[];
}>;
export declare function AutoFormField(props: AutoFormFieldProps): JSX.Element | null;
export {};
//# sourceMappingURL=autoform-field.d.ts.map