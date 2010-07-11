//
//  HTTPUtil2.m
//  TJClient
//
//  Created by Katsunobu Ishida on 10/06/28.
//  Copyright 2010 Katsunobu Ishida. All rights reserved.
//

#import "HTTPUtil.h"


@implementation HTTPUtil

- (void)sendAsyncGetRequestWithURL:(NSURL*)url 
				   timeoutInterval:(NSTimeInterval)timeout
								to:(id)delegate 
						  selector:(SEL)selector {
	
	resultHandler = [delegate retain];
	resultSelector = selector;
	responseData = [[NSMutableData data] retain];
	NSURLRequest *request = [NSURLRequest requestWithURL:url 
											 cachePolicy:NSURLRequestReloadIgnoringCacheData 
										 timeoutInterval:timeout];
	
	[[NSURLConnection alloc] initWithRequest:request delegate:self];
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
	[responseData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
	[responseData appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
	[connection release];
	[resultHandler performSelector:resultSelector withObject:self withObject:nil];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
	[connection cancel];
	[connection release];
	NSString *responseString = [[NSString alloc] initWithData:responseData 
													 encoding:NSUTF8StringEncoding];
	[resultHandler performSelector:resultSelector withObject:self withObject:responseString];
}

- (void)dealloc {
	[responseData release];
	[resultHandler release];
    
	[super dealloc];
}

@end
