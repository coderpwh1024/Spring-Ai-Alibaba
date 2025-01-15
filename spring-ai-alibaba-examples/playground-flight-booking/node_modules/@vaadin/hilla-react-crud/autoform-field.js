import { jsx } from "react/jsx-runtime";
import { _enum } from "@vaadin/hilla-lit-form";
import { useFormPart } from "@vaadin/hilla-react-form";
import { Checkbox } from "@vaadin/react-components/Checkbox.js";
import { DatePicker } from "@vaadin/react-components/DatePicker.js";
import { DateTimePicker } from "@vaadin/react-components/DateTimePicker.js";
import { IntegerField } from "@vaadin/react-components/IntegerField.js";
import { NumberField } from "@vaadin/react-components/NumberField.js";
import { Select } from "@vaadin/react-components/Select.js";
import { TextArea } from "@vaadin/react-components/TextArea.js";
import { TextField } from "@vaadin/react-components/TextField.js";
import { TimePicker } from "@vaadin/react-components/TimePicker.js";
import {
  cloneElement,
  createElement,
  useEffect,
  useMemo
} from "react";
import { useDatePickerI18n, useDateTimePickerI18n } from "./locale.js";
import { convertToTitleCase } from "./util.js";
function getPropertyModel(form, propertyInfo) {
  const pathParts = propertyInfo.name.split(".");
  return pathParts.reduce((model, property) => model ? model[property] : void 0, form.model);
}
function renderFieldElement(defaultComponentType, { element, field, fieldProps }, additionalProps = {}) {
  const fieldElement = element ?? createElement(defaultComponentType);
  return cloneElement(fieldElement, { ...fieldProps, ...additionalProps, ...fieldElement.props, ...field });
}
function AutoFormTextField(props) {
  return renderFieldElement(TextField, props);
}
function AutoFormIntegerField(props) {
  return renderFieldElement(IntegerField, props);
}
function AutoFormDecimalField(props) {
  return renderFieldElement(NumberField, props);
}
function AutoFormDateField(props) {
  const i18n = useDatePickerI18n();
  return renderFieldElement(DatePicker, props, { i18n });
}
function AutoFormTimeField(props) {
  return renderFieldElement(TimePicker, props);
}
function AutoFormDateTimeField(props) {
  const i18n = useDateTimePickerI18n();
  return renderFieldElement(DateTimePicker, props, { i18n });
}
function AutoFormEnumField(props) {
  const enumModel = props.model;
  const items = Object.keys(enumModel[_enum]).map((value) => ({
    label: convertToTitleCase(value),
    value
  }));
  return renderFieldElement(Select, props, { items });
}
function AutoFormBooleanField(props) {
  return renderFieldElement(Checkbox, props);
}
function AutoFormObjectField({ model, fieldProps }) {
  const part = useFormPart(model);
  const jsonString = part.value ? JSON.stringify(part.value) : "";
  return /* @__PURE__ */ jsx(TextArea, { ...fieldProps, value: jsonString, readonly: true });
}
function AutoFormField(props) {
  const { form, propertyInfo, options } = props;
  const label = options.label ?? propertyInfo.humanReadableName;
  const model = getPropertyModel(form, propertyInfo);
  const field = form.field(model);
  const formPart = useFormPart(model);
  const defaultValidators = useMemo(() => formPart.validators, []);
  const { validators } = options;
  useEffect(() => {
    formPart.setValidators([...defaultValidators, ...validators ?? []]);
  }, [validators]);
  if (options.renderer) {
    const customFieldProps = { ...field, disabled: props.disabled, label };
    return options.renderer({ field: customFieldProps });
  }
  const fieldProps = {
    id: options.id,
    className: options.className,
    style: options.style,
    label,
    placeholder: options.placeholder,
    helperText: options.helperText,
    colspan: options.colspan,
    disabled: options.disabled ?? props.disabled,
    readonly: options.readonly
  };
  const rendererProps = { model, field, element: options.element, fieldProps };
  switch (props.propertyInfo.type) {
    case "string":
      return /* @__PURE__ */ jsx(AutoFormTextField, { ...rendererProps });
    case "integer":
      return /* @__PURE__ */ jsx(AutoFormIntegerField, { ...rendererProps });
    case "decimal":
      return /* @__PURE__ */ jsx(AutoFormDecimalField, { ...rendererProps });
    case "date":
      return /* @__PURE__ */ jsx(AutoFormDateField, { ...rendererProps });
    case "time":
      return /* @__PURE__ */ jsx(AutoFormTimeField, { ...rendererProps });
    case "datetime":
      return /* @__PURE__ */ jsx(AutoFormDateTimeField, { ...rendererProps });
    case "enum":
      return /* @__PURE__ */ jsx(AutoFormEnumField, { ...rendererProps });
    case "boolean":
      return /* @__PURE__ */ jsx(AutoFormBooleanField, { ...rendererProps });
    case "object":
      return /* @__PURE__ */ jsx(AutoFormObjectField, { ...rendererProps });
    default:
      return null;
  }
}
export {
  AutoFormField
};
//# sourceMappingURL=autoform-field.js.map
