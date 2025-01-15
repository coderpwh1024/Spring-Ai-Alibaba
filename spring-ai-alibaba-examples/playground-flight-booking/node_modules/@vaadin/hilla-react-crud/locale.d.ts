/// <reference types="react" />
import { type DatePickerDate, type DatePickerI18n } from '@vaadin/react-components/DatePicker.js';
import { type DateTimePickerI18n } from '@vaadin/react-components/DateTimePicker.js';
export declare const LocaleContext: import("react").Context<string>;
export declare class LocaleFormatter {
    #private;
    constructor(locale?: string);
    formatDate(value?: string): string;
    formatDate(value: DatePickerDate): string;
    formatLocalTime(value?: string): string;
    formatLocalDateTime(value?: string): string;
    formatInteger(value?: number): string;
    formatDecimal(value?: number): string;
    parse(dateString: string): DatePickerDate | undefined;
}
export declare function useLocaleFormatter(): LocaleFormatter;
export declare function useDatePickerI18n(): DatePickerI18n;
export declare function useDateTimePickerI18n(): DateTimePickerI18n;
//# sourceMappingURL=locale.d.ts.map