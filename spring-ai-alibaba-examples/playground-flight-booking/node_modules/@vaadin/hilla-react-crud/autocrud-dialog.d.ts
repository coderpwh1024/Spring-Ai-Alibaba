import type { JSX } from 'react';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
interface AutoCrudDialogProps {
    header: JSX.Element | null | undefined;
    children: React.ReactElement;
    opened: boolean;
    onClose: () => void;
}
export declare function AutoCrudDialog(props: AutoCrudDialogProps): JSX.Element;
export {};
//# sourceMappingURL=autocrud-dialog.d.ts.map