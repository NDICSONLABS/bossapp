import jspreadsheet from 'jspreadsheet-ce';
import jSuites from 'jsuites';

import 'jspreadsheet-ce/dist/jspreadsheet.css';
import 'jsuites/dist/jsuites.css';

window.jspreadsheet = jspreadsheet;
window.jSuites = jSuites;

class JSpreadsheetConnector {
    private host: HTMLElement;
    private container: HTMLElement;
    private instance: any[] | null = null;
    private options: any;
    private debounceHandle: number | undefined;
    private isRemoteUpdate: boolean = false;

    constructor(host: HTMLElement, container: HTMLElement) {
        this.host = host;
        this.container = container;
    }

    create(options: any = {}) {
        this.destroy();
        this.options = this.decorateOptions(options);
        this.instance = jspreadsheet(this.container, this.options);
        this.dispatch('jspreadsheet-ready', { type: 'ready' });
    }

    destroy() {
        if (this.instance) {
            try { jspreadsheet.destroy(this.container, true); } catch (e) {}
        }
        this.instance = null;
        this.container.innerHTML = '';
    }

    applyRemoteChange(worksheetIndex: number, x: number, y: number, value: any) {
        const ws = this.getWorksheet(worksheetIndex);
        if (!ws) return;

        const cellName = this.toCellName(y, x); // Convert 0-based (row, col) to Excel format (A1)

        this.isRemoteUpdate = true;
        try {
            if (typeof ws.setValue === 'function') {
                // setValue(cellIdent, value, force)
                ws.setValue(cellName, value, true);
            }
        } catch (e) {
            console.error("Error applying remote change", e);
        } finally {
            this.isRemoteUpdate = false;
        }
    }

    private decorateOptions(options: any) {
        const decorated = { ...options };
        if (!decorated.worksheets) decorated.worksheets = [{ minDimensions: [10, 10] }];

        // Explicitly wire onchange for cell-level collaboration
        const userOnchange = decorated.onchange;
        decorated.onchange = (worksheet: any, cell: any, x: number, y: number, newValue: any, oldValue: any) => {
            if (this.isRemoteUpdate) return; // Prevent echo loops

            const wsIndex = this.getWorksheetIndex(worksheet);
            this.dispatch('jspreadsheet-cell-changed', {
                type: 'cell-changed',
                worksheetIndex: wsIndex,
                x: x, y: y, value: newValue, oldValue: oldValue
            });

            this.fireChanged('onchange', wsIndex);
            if (typeof userOnchange === 'function') userOnchange(worksheet, cell, x, y, newValue, oldValue);
        };

        return decorated;
    }

    private getWorksheetIndex(worksheet: any) {
        return this.instance ? this.instance.indexOf(worksheet) : 0;
    }

    private getWorksheet(worksheetIndex: number = 0) {
        if (!this.instance || !this.instance[worksheetIndex]) throw new Error(`Worksheet not available: ${worksheetIndex}`);
        return this.instance[worksheetIndex];
    }

    private fireChanged(reason: string, worksheetIndex?: number) {
        // Debounce full-state sync
        if (this.debounceHandle) window.clearTimeout(this.debounceHandle);
        this.debounceHandle = window.setTimeout(() => {
            this.dispatch('jspreadsheet-changed', { type: reason, worksheetIndex, data: this.getWorkbook() });
        }, 250);
    }

    private dispatch(eventName: string, payload: any) {
        this.host.dispatchEvent(new CustomEvent(eventName, {
            bubbles: true, composed: true, detail: { ...payload, json: JSON.stringify(payload.data ?? payload) }
        }));
    }

    private getWorkbook() { /* ... (same as previous implementation) ... */ }

    private toCellName(row: number, col: number) {
        let columnName = '';
        let c = col + 1;
        while (c > 0) {
            const mod = (c - 1) % 26;
            columnName = String.fromCharCode(65 + mod) + columnName;
            c = Math.floor((c - mod) / 26);
        }
        return `${columnName}${row + 1}`;
    }
}

window.JSpreadsheetConnector = JSpreadsheetConnector;
export { JSpreadsheetConnector };

// import jspreadsheet from 'jspreadsheet-ce';
// import jSuites from 'jsuites';
//
// import 'jspreadsheet-ce/dist/jspreadsheet.css';
// import 'jsuites/dist/jsuites.css';
//
// // Optional, depending on your visual requirements.
// // Jspreadsheet examples often use Material Icons.
// // For production/offline usage prefer @fontsource/material-icons instead of Google CDN.
// // import '@fontsource/material-icons';
//
// declare global {
//     interface Window {
//         JSpreadsheetConnector: typeof JSpreadsheetConnector;
//         jspreadsheet: any;
//         jSuites: any;
//     }
// }
//
// // Some builds/extensions expect globals.
// window.jspreadsheet = jspreadsheet;
// window.jSuites = jSuites;
//
// type ListenerPayload = {
//     type: string;
//     worksheetIndex?: number;
//     args?: any[];
//     data?: any;
// };
//
// class JSpreadsheetConnector {
//     private host: HTMLElement;
//     private container: HTMLElement;
//     private instance: any[] | null = null;
//     private options: any;
//     private debounceHandle: number | undefined;
//
//     constructor(host: HTMLElement, container: HTMLElement) {
//         this.host = host;
//         this.container = container;
//     }
//
//     create(options: any = {}) {
//         this.destroy();
//
//         this.options = this.decorateOptions(options);
//
//         this.instance = jspreadsheet(this.container, this.options);
//
//         this.dispatch('jspreadsheet-ready', {
//             type: 'ready',
//             data: this.getWorkbook()
//         });
//     }
//
//     destroy() {
//         if (this.instance) {
//             try {
//                 jspreadsheet.destroy(this.container, true);
//             } catch (e) {
//                 console.warn('[JSpreadsheetConnector] destroy failed', e);
//             }
//         }
//
//         this.instance = null;
//         this.container.innerHTML = '';
//     }
//
//     getWorkbook() {
//         if (!this.instance) {
//             return { worksheets: [] };
//         }
//
//         const worksheets = this.instance.map((worksheet: any, index: number) => {
//             const config = worksheet.options ? { ...worksheet.options } : {};
//
//             return {
//                 ...config,
//                 data: typeof worksheet.getData === 'function'
//                     ? worksheet.getData()
//                     : config.data,
//                 worksheetIndex: index
//             };
//         });
//
//         return { worksheets };
//     }
//
//     setWorkbook(workbook: any) {
//         const options = {
//             ...this.options,
//             worksheets: workbook?.worksheets || []
//         };
//
//         this.create(options);
//     }
//
//     getData(worksheetIndex: number = 0) {
//         const ws = this.getWorksheet(worksheetIndex);
//         return ws.getData();
//     }
//
//     setData(data: any[][], worksheetIndex: number = 0) {
//         const ws = this.getWorksheet(worksheetIndex);
//
//         if (typeof ws.setData === 'function') {
//             ws.setData(data);
//         } else {
//             this.setWorkbook({
//                 worksheets: [
//                     {
//                         ...(this.options?.worksheets?.[worksheetIndex] || {}),
//                         data
//                     }
//                 ]
//             });
//         }
//
//         this.fireChanged('setData', worksheetIndex);
//     }
//
//     getValue(row: number, col: number, worksheetIndex: number = 0) {
//         const ws = this.getWorksheet(worksheetIndex);
//
//         if (typeof ws.getValueFromCoords === 'function') {
//             return ws.getValueFromCoords(col, row);
//         }
//
//         const data = ws.getData();
//         return data?.[row]?.[col];
//     }
//
//     setValue(row: number, col: number, value: any, worksheetIndex: number = 0) {
//         const ws = this.getWorksheet(worksheetIndex);
//
//         if (typeof ws.setValueFromCoords === 'function') {
//             ws.setValueFromCoords(col, row, value);
//         } else {
//             const cellName = this.toCellName(row, col);
//             ws.setValue(cellName, value);
//         }
//
//         this.fireChanged('setValue', worksheetIndex);
//     }
//
//     /**
//      * Generic passthrough.
//      *
//      * target:
//      * - "workbook": call static/global jspreadsheet function if present
//      * - "worksheet": call worksheet instance method
//      */
//     exec(
//         methodName: string,
//         args: any[] = [],
//         worksheetIndex: number = 0,
//         target: 'worksheet' | 'global' = 'worksheet'
//     ): any {
//         if (target === 'global') {
//             const fn = (jspreadsheet as any)[methodName];
//             if (typeof fn !== 'function') {
//                 throw new Error(`Global jspreadsheet method not found: ${methodName}`);
//             }
//             return fn.apply(jspreadsheet, args);
//         }
//
//         const ws = this.getWorksheet(worksheetIndex);
//         const fn = ws[methodName];
//
//         if (typeof fn !== 'function') {
//             throw new Error(`Worksheet method not found: ${methodName}`);
//         }
//
//         return fn.apply(ws, args);
//     }
//
//     private getWorksheet(worksheetIndex: number = 0) {
//         if (!this.instance || !this.instance[worksheetIndex]) {
//             throw new Error(`Worksheet not available: ${worksheetIndex}`);
//         }
//
//         return this.instance[worksheetIndex];
//     }
//
//     private decorateOptions(options: any) {
//         const decorated = { ...options };
//
//         if (!decorated.worksheets) {
//             decorated.worksheets = [{ minDimensions: [10, 10] }];
//         }
//
//         /*
//          * Jspreadsheet supports a centralized event callback called `onevent`.
//          * The CE docs/changelog mention centralized event dispatch through
//          * `onevent` [[1]].
//          */
//         const userOnevent = decorated.onevent;
//
//         decorated.onevent = (...args: any[]) => {
//             const eventName = args?.[0];
//
//             this.dispatch('jspreadsheet-event', {
//                 type: eventName,
//                 args
//             });
//
//             if (this.isMutatingEvent(eventName)) {
//                 this.fireChanged(eventName);
//             }
//
//             if (typeof userOnevent === 'function') {
//                 return userOnevent(...args);
//             }
//         };
//
//         /*
//          * Also wire common direct callbacks because many Jspreadsheet examples
//          * use named callbacks such as onchange.
//          */
//         this.wrapCallback(decorated, 'onchange', true);
//         this.wrapCallback(decorated, 'oninsertrow', true);
//         this.wrapCallback(decorated, 'ondeleterow', true);
//         this.wrapCallback(decorated, 'oninsertcolumn', true);
//         this.wrapCallback(decorated, 'ondeletecolumn', true);
//         this.wrapCallback(decorated, 'onselection', false);
//         this.wrapCallback(decorated, 'onload', false);
//
//         return decorated;
//     }
//
//     private wrapCallback(options: any, name: string, mutating: boolean) {
//         const original = options[name];
//
//         options[name] = (...args: any[]) => {
//             this.dispatch('jspreadsheet-event', {
//                 type: name,
//                 args
//             });
//
//             if (mutating) {
//                 this.fireChanged(name);
//             }
//
//             if (typeof original === 'function') {
//                 return original(...args);
//             }
//         };
//     }
//
//     private fireChanged(reason: string, worksheetIndex?: number) {
//         if (this.debounceHandle) {
//             window.clearTimeout(this.debounceHandle);
//         }
//
//         this.debounceHandle = window.setTimeout(() => {
//             this.dispatch('jspreadsheet-changed', {
//                 type: reason,
//                 worksheetIndex,
//                 data: this.getWorkbook()
//             });
//         }, 250);
//     }
//
//     private dispatch(eventName: string, payload: ListenerPayload) {
//         this.host.dispatchEvent(new CustomEvent(eventName, {
//             bubbles: true,
//             composed: true,
//             detail: {
//                 ...payload,
//                 json: JSON.stringify(payload.data ?? payload)
//             }
//         }));
//     }
//
//     private isMutatingEvent(eventName: string) {
//         if (!eventName) {
//             return false;
//         }
//
//         return [
//             'onchange',
//             'onafterchanges',
//             'oninsertrow',
//             'ondeleterow',
//             'oninsertcolumn',
//             'ondeletecolumn',
//             'onmoverow',
//             'onmovecolumn',
//             'onmerge',
//             'ondelete'
//         ].includes(eventName);
//     }
//
//     private toCellName(row: number, col: number) {
//         let columnName = '';
//         let c = col + 1;
//
//         while (c > 0) {
//             const mod = (c - 1) % 26;
//             columnName = String.fromCharCode(65 + mod) + columnName;
//             c = Math.floor((c - mod) / 26);
//         }
//
//         return `${columnName}${row + 1}`;
//     }
// }
//
// window.JSpreadsheetConnector = JSpreadsheetConnector;
//
// export { JSpreadsheetConnector };