//
//  TJClientViewController.m
//  TJClient
//
//  Created by Katsunobu Ishida on 10/06/27.
//  Copyright Katsunobu Ishida 2010. All rights reserved.
//

#import "TJClientViewController.h"
#import	"HTTPUtil.h"

#define kActionNotDetected -1
#define kActionUp 1
#define kActionRight 2
#define kActionDown 3
#define kActionLeft 4
#define kActionJog 5
#define kActionZoomIn 6
#define kActionZoomOut 7

#define kAccelerometerInterval 0.5f
#define kSendingTimeoutInterval 10.0
#define kDefaultUrl @"http://localhost:8081/command"
#define kDefaultUrlKey @"urlText"
#define kConnectionStatusConnecting @"Connecting"
#define kConnectionStatusNotConnecting @"Not connecting"


@interface TJClientViewController (Private)
- (void)initializeAccelerometer;
- (void)sendWithActionId:(int)actionId;
- (void)sendWithXAcceleration:(double)xa yAcceleration:(double)ya zAcceleration:(double)za;
- (void)sendWithActionId:(int)action xAcceleration:(double)xa yAcceleration:(double)ya zAcceleration:(double)za;
@end

@implementation TJClientViewController
@synthesize urlTextField;
@synthesize connectionStatusLabel;
@synthesize accelerometerStatusLabel;

#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
	
    NSString *defaultURLText = 
	 [[NSUserDefaults standardUserDefaults] stringForKey:kDefaultUrlKey];
    if (defaultURLText == nil) {
		defaultURLText = kDefaultUrl;
    }
    urlTextField.text = defaultURLText;
	urlTextField.delegate = self;
    connectionStatusLabel.text = kConnectionStatusNotConnecting;
	canSend = YES;
	[self initializeAccelerometer];
}

#pragma mark Memory management

- (void)dealloc {
    [urlTextField release];
    [connectionStatusLabel release];
    [accelerometerStatusLabel release];
    [infoViewController release];
    [accelerometer release];
    [super dealloc];
}

#pragma mark -
#pragma mark UI action

- (IBAction)sendActionLeft:(id)sender {	
	[self sendWithActionId:kActionLeft];
}

- (IBAction)sendActionRight:(id)sender {
	[self sendWithActionId:kActionRight];
}

- (IBAction)sendActionUp:(id)sender {
	[self sendWithActionId:kActionUp];
}

- (IBAction)sendActionDown:(id)sender {
	[self sendWithActionId:kActionDown];
}

- (IBAction)sendActionZoomIn:(id)sender {
	[self sendWithActionId:kActionZoomIn];
}

- (IBAction)sendActionZoomOut:(id)sender {
	[self sendWithActionId:kActionZoomOut];
}

- (IBAction)sendActionJog:(id)sender {
	[self sendWithActionId:kActionJog];
}

- (IBAction)showInfoView:(id)sender {
	if (infoViewController == nil) {
		infoViewController = [[InfoViewController alloc] init];
	}
	infoViewController.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
	[self presentModalViewController:infoViewController animated:YES];
}

#pragma mark UITextField delegate

- (void)textFieldDidEndEditing:(UITextField *)textField {
    NSString *newValue;
    NSString *oldValue;
    
    newValue = urlTextField.text;
    oldValue = [[NSUserDefaults standardUserDefaults] stringForKey:kDefaultUrlKey];
    
    if (   ((oldValue == nil) && ! [newValue isEqualToString:kDefaultUrl] ) 
        || ((oldValue != nil) && ! [newValue isEqualToString:oldValue] ) ) {
        [[NSUserDefaults standardUserDefaults] setObject:newValue forKey:kDefaultUrlKey];
    }
	
	canSend = YES;
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
	canSend = NO;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

#pragma mark UIAccelerometer helper and delegate

- (void)initializeAccelerometer
{
 	accelerometer = [UIAccelerometer sharedAccelerometer];
 	accelerometer.updateInterval = kAccelerometerInterval;
 	accelerometer.delegate = self;
}

- (void)accelerometer:(UIAccelerometer *)accelerometer
		didAccelerate:(UIAcceleration *)acceleration {
	accelerometerStatusLabel.text = 
	 [NSString stringWithFormat:@"Accelerometer: %f, %f, %f", 
	  acceleration.x, acceleration.y, acceleration.z];
	[self sendWithXAcceleration:acceleration.x yAcceleration:acceleration.y zAcceleration:acceleration.z];
}

#pragma mark HTTPUtil helper and delegate

- (void)sendWithActionId:(int)action {
	[self sendWithActionId:action xAcceleration:0.0 yAcceleration:0.0 zAcceleration:0.0];
}

- (void)sendWithXAcceleration:(double)xa yAcceleration:(double)ya zAcceleration:(double)za {
	[self sendWithActionId:kActionNotDetected xAcceleration:xa yAcceleration:ya zAcceleration:za];
}

- (void)sendWithActionId:(int)actionId xAcceleration:(double)xa yAcceleration:(double)ya zAcceleration:(double)za {
	if (!canSend || [urlTextField.text isEqual:@""]) {
		connectionStatusLabel.text = kConnectionStatusNotConnecting;
		connectionStatusLabel.textColor = [UIColor lightGrayColor];
		return;
	}
	
	HTTPUtil *http = [[HTTPUtil alloc] init];
	NSString *urlString = (actionId < 0) 
	 ? [NSString stringWithFormat:@"%@?xa=%f&ya=%f&za=%f", 
		urlTextField.text, xa, ya, za] 
	 : [NSString stringWithFormat:@"%@?xa=%f&ya=%f&za=%f&action=%d", 
		urlTextField.text, xa, ya, za, actionId];
	[http sendAsyncGetRequestWithURL:[NSURL URLWithString:urlString] 
					 timeoutInterval:kSendingTimeoutInterval 
								  to:self 
							selector:@selector(http:didReceiveAsyncGetReplyWithResult:)];
}

- (void)http:(HTTPUtil *)http didReceiveAsyncGetReplyWithResult:(NSString*)result {
	[http release];
	if (result) {
		connectionStatusLabel.text = kConnectionStatusConnecting;
		connectionStatusLabel.textColor = 
		 [UIColor colorWithRed:67/255.0 green:194/255.0 blue:249/255.0 alpha:1.0];
	} else {
		connectionStatusLabel.text = kConnectionStatusNotConnecting;
		connectionStatusLabel.textColor = [UIColor lightGrayColor];
	}
}

@end
