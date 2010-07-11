//
//  TJClientAppDelegate.m
//  TJClient
//
//  Created by Katsunobu Ishida on 10/06/27.
//  Copyright Katsunobu Ishida 2010. All rights reserved.
//

#import "TJClientAppDelegate.h"
#import "TJClientViewController.h"

@implementation TJClientAppDelegate

@synthesize window;
@synthesize viewController;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    // Override point for customization after app launch    
    [window addSubview:viewController.view];
    [window makeKeyAndVisible];
	
	return YES;
}


- (void)dealloc {
    [viewController release];
    [window release];
    [super dealloc];
}


@end
