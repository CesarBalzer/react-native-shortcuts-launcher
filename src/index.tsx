import { NativeModules } from 'react-native';

const ShortcutsLauncher = NativeModules.ShortcutsLauncher;

export function checkPermissionToCall() {
  return ShortcutsLauncher.checkPermissionToCall();
}
export function addDetailToShortcut(a: any) {
  return ShortcutsLauncher.addDetailToShortcut(a);
}

export function updateShortcut(a: any){
  return ShortcutsLauncher.updateShortcut(a);
}

export function addShortcutToScreen(a: any): Promise<any> {
  return ShortcutsLauncher.addShortcutToScreen(a);
}

export function addPinnedShortcut(a: any): Promise<any> {
  return ShortcutsLauncher.addPinnedShortcut(a);
}

export function exists(a: any): Promise<string> {
  return ShortcutsLauncher.exists(a);
}

export function getDrawableImageNames(): Promise<string[]> {
  return ShortcutsLauncher.getDrawableImageNames();
}
export function multiply(a: number, b: number): Promise<number> {
  return ShortcutsLauncher.multiply(a, b);
}
