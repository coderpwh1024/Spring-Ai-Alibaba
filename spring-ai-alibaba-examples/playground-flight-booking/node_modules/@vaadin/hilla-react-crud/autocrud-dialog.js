import { jsx, jsxs } from "react/jsx-runtime";
import { Button } from "@vaadin/react-components/Button";
import { Dialog } from "@vaadin/react-components/Dialog";
import { Icon } from "@vaadin/react-components/Icon";
import "@vaadin/vaadin-lumo-styles/vaadin-iconset.js";
function AutoCrudDialog(props) {
  const { header, children, opened, onClose } = props;
  return /* @__PURE__ */ jsx(
    Dialog,
    {
      overlayClass: "auto-crud-dialog",
      opened,
      headerRenderer: () => /* @__PURE__ */ jsxs("div", { className: "auto-crud-dialog-header", children: [
        header,
        /* @__PURE__ */ jsx(Button, { theme: "tertiary", onClick: onClose, "aria-label": "Close", children: /* @__PURE__ */ jsx(Icon, { icon: "lumo:cross", style: { height: "var(--lumo-icon-size-l)", width: "var(--lumo-icon-size-l)" } }) })
      ] }),
      children
    }
  );
}
export {
  AutoCrudDialog
};
//# sourceMappingURL=autocrud-dialog.js.map
