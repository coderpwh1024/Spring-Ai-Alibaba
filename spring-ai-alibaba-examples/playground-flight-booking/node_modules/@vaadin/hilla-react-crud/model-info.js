import {
  BooleanModel,
  NumberModel,
  StringModel,
  _meta,
  createDetachedModel,
  EnumModel,
  ObjectModel
} from "@vaadin/hilla-lit-form";
const javaTypeMap = {
  byte: "integer",
  "java.lang.Byte": "integer",
  short: "integer",
  "java.lang.Short": "integer",
  int: "integer",
  "java.lang.Integer": "integer",
  long: "integer",
  "java.lang.Long": "integer",
  float: "decimal",
  "java.lang.Float": "decimal",
  double: "decimal",
  "java.lang.Double": "decimal",
  "java.time.LocalDate": "date",
  "java.time.LocalTime": "time",
  "java.time.LocalDateTime": "datetime"
};
function determinePropertyType(model) {
  const { javaType } = model[_meta];
  const propertyType = javaType ? javaTypeMap[javaType] : void 0;
  if (propertyType) {
    return propertyType;
  }
  if (model instanceof StringModel) {
    return "string";
  } else if (model instanceof NumberModel) {
    return "decimal";
  } else if (model instanceof BooleanModel) {
    return "boolean";
  } else if (model instanceof EnumModel) {
    return "enum";
  } else if (model instanceof ObjectModel) {
    return "object";
  }
  return void 0;
}
function hasAnnotation(meta, annotationName) {
  return meta.annotations?.some((annotation) => annotation.name === annotationName) ?? false;
}
function _generateHeader(path) {
  return path.substring(path.lastIndexOf(".") + 1).replace(/([A-Z])/gu, "-$1").toLowerCase().replace(/-/gu, " ").replace(/^./u, (match) => match.toUpperCase());
}
const getPropertyNames = (model) => {
  const propertyNames = [];
  for (let proto = model; proto !== ObjectModel; proto = Object.getPrototypeOf(proto)) {
    propertyNames.unshift(...Object.keys(Object.getOwnPropertyDescriptors(proto.prototype)).filter((p) => p !== "new"));
  }
  return propertyNames;
};
class ModelInfo {
  modelInstance;
  idProperty;
  constructor(model, idPropertyName) {
    this.modelInstance = createDetachedModel(model);
    this.idProperty = ModelInfo.resolveIdProperty(this, idPropertyName);
  }
  static resolveIdProperty(modelInfo, idPropertyName) {
    if (idPropertyName) {
      return modelInfo.getProperty(idPropertyName);
    }
    const rootProperties = modelInfo.getRootProperties();
    let idProperty = rootProperties.find((propertyInfo) => hasAnnotation(propertyInfo.meta, "jakarta.persistence.Id"));
    if (!idProperty) {
      idProperty = rootProperties.find((propertyInfo) => propertyInfo.name === "id");
    }
    return idProperty;
  }
  static resolvePropertyModel(modelInstance, path) {
    const parts = path.split(".");
    let currentModel = modelInstance;
    for (const part of parts) {
      if (!currentModel || !(currentModel instanceof ObjectModel)) {
        return void 0;
      }
      currentModel = currentModel[part];
    }
    return currentModel;
  }
  getRootProperties(path) {
    const model = path ? ModelInfo.resolvePropertyModel(this.modelInstance, path) : this.modelInstance;
    if (!model) {
      return [];
    }
    return getPropertyNames(model.constructor).map((name) => {
      const effectivePath = path ? `${path}.${name}` : name;
      return this.getProperty(effectivePath);
    }).filter(Boolean);
  }
  getProperty(path) {
    const propertyModel = ModelInfo.resolvePropertyModel(this.modelInstance, path);
    if (!propertyModel?.[_meta]) {
      return void 0;
    }
    const pathParts = path.split(".");
    const name = pathParts[pathParts.length - 1];
    const meta = propertyModel[_meta];
    const humanReadableName = _generateHeader(name);
    const type = determinePropertyType(propertyModel);
    return {
      name: path,
      humanReadableName,
      type,
      meta,
      model: propertyModel
    };
  }
  getProperties(paths) {
    return paths.map((path) => this.getProperty(path)).filter(Boolean);
  }
}
function getDefaultProperties(modelInfo) {
  const properties = modelInfo.getRootProperties();
  return properties.flatMap((prop) => {
    if (hasAnnotation(prop.meta, "jakarta.persistence.OneToOne")) {
      return modelInfo.getRootProperties(prop.name);
    }
    return prop;
  }).filter(
    (prop) => !!prop.type && !(hasAnnotation(prop.meta, "jakarta.persistence.Id") || hasAnnotation(prop.meta, "jakarta.persistence.Version"))
  );
}
export {
  ModelInfo,
  _generateHeader,
  getDefaultProperties,
  hasAnnotation
};
//# sourceMappingURL=model-info.js.map
