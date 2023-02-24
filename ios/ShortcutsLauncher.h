
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNShortcutsLauncherSpec.h"

@interface ShortcutsLauncher : NSObject <NativeShortcutsLauncherSpec>
#else
#import <React/RCTBridgeModule.h>

@interface ShortcutsLauncher : NSObject <RCTBridgeModule>
#endif

@end
