import { DatePickerElement } from "@vaadin/react-components/DatePicker.js";
import { DateTimePickerElement } from "@vaadin/react-components/DateTimePicker.js";
import { createContext, useContext, useMemo } from "react";
const LocaleContext = createContext(navigator.language);
function getFormatRegex(format) {
  const sampleDate = new Date(1234, 5 - 1, 6);
  const formattedSample = format.format(sampleDate);
  const pattern = formattedSample.replace("1234", "(?<year>\\d+)").replace("05", "(?<month>\\d+)").replace("5", "(?<month>\\d+)").replace("06", "(?<day>\\d+)").replace("6", "(?<day>\\d+)");
  return new RegExp(pattern, "u");
}
function tryFormatDate(formatter, value) {
  try {
    return value ? formatter.format(new Date(value)) : "";
  } catch (e) {
    return "";
  }
}
class LocaleFormatter {
  #date;
  #localTime;
  #localDateTime;
  #integer;
  #decimal;
  #parsePattern;
  constructor(locale) {
    this.#date = new Intl.DateTimeFormat(locale);
    this.#localTime = new Intl.DateTimeFormat(locale, {
      hour: "numeric",
      minute: "numeric"
    });
    this.#localDateTime = new Intl.DateTimeFormat(locale, {
      day: "numeric",
      month: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "numeric"
    });
    this.#integer = new Intl.NumberFormat(locale, {
      maximumFractionDigits: 0
    });
    this.#decimal = new Intl.NumberFormat(locale, {
      maximumFractionDigits: 2,
      minimumFractionDigits: 2
    });
    this.#parsePattern = getFormatRegex(this.#date);
  }
  formatDate(value) {
    if (typeof value === "object") {
      const { year, month, day } = value;
      const date = /* @__PURE__ */ new Date();
      date.setFullYear(year);
      date.setMonth(month);
      date.setDate(day);
      return this.#date.format(date);
    }
    return tryFormatDate(this.#date, value);
  }
  formatLocalTime(value) {
    return tryFormatDate(this.#localTime, `2000-01-01T${value}`);
  }
  formatLocalDateTime(value) {
    return tryFormatDate(this.#localDateTime, value);
  }
  formatInteger(value) {
    return value && Number.isFinite(value) ? this.#integer.format(value) : "";
  }
  formatDecimal(value) {
    return value && Number.isFinite(value) ? this.#decimal.format(value) : "";
  }
  parse(dateString) {
    const match = this.#parsePattern.exec(dateString);
    const year = Number(match?.groups?.year);
    const month = Number(match?.groups?.month) - 1;
    const day = Number(match?.groups?.day);
    const dateInstance = /* @__PURE__ */ new Date();
    dateInstance.setFullYear(year);
    dateInstance.setMonth(month);
    dateInstance.setDate(day);
    if (dateInstance.getFullYear() !== year || dateInstance.getMonth() !== month || dateInstance.getDate() !== day) {
      return void 0;
    }
    return { year, month, day };
  }
}
function useLocaleFormatter() {
  const locale = useContext(LocaleContext);
  return useMemo(() => new LocaleFormatter(locale), [locale]);
}
const defaultDatePickerI18n = new DatePickerElement().i18n;
function useDatePickerI18n() {
  const formatter = useLocaleFormatter();
  return useMemo(
    () => ({
      ...defaultDatePickerI18n,
      formatDate(value) {
        return formatter.formatDate(value);
      },
      parseDate(value) {
        return formatter.parse(value);
      }
    }),
    [formatter]
  );
}
const defaultDateTimePickerI18n = new DateTimePickerElement().i18n;
function useDateTimePickerI18n() {
  const datePickerI18n = useDatePickerI18n();
  return useMemo(
    () => ({
      ...defaultDateTimePickerI18n,
      ...datePickerI18n
    }),
    [datePickerI18n]
  );
}
export {
  LocaleContext,
  LocaleFormatter,
  useDatePickerI18n,
  useDateTimePickerI18n,
  useLocaleFormatter
};
//# sourceMappingURL=locale.js.map
