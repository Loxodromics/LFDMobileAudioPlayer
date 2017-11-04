//
//  LFDAudioPlayer.m
//  LFD Audio Player
//
//  Created by -philipp on 26.06.14.
//  Copyright (c) 2014 Quatur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "LFDAudioPlayer.h"
#import "LFDAudioPlayerConstants.h"

@interface LFDAudioPlayer()

@property (strong, nonatomic) AVPlayer* audioPlayer;
@property (strong, nonatomic) AVPlayerItem* audioPlayerItem;
@property (strong, nonatomic) NSURL* currentStationUrl;
@property (strong, nonatomic) NSTimer* reconnectTimer;
@property (strong, nonatomic) NSTimer* fadeTimer;
@property (nonatomic) NSUInteger reconnectCounter;
@property (nonatomic) BOOL isInterupted;

@end

@implementation LFDAudioPlayer

UIBackgroundTaskIdentifier task;

+ (LFDAudioPlayer*)sharedInstance
{
	static LFDAudioPlayer* mySharedInstance = nil;
	static dispatch_once_t onceToken;

	dispatch_once(&onceToken, ^{
		mySharedInstance = [[self alloc] init];
	});
	return mySharedInstance;
}

- (id)init
{
	if (self = [super init])
	{

	}
	return self;
}

- (void)dealloc
{
	// Should never be called, but just here for clarity really.
}

#pragma mark - playback

- (void)play:(NSURL*)stationUrl
{
/// how to swap the scheme to https
//    NSURLComponents *originalComponents = [NSURLComponents componentsWithURL:stationUrl resolvingAgainstBaseURL:YES];
//
//    NSURLComponents *components = [NSURLComponents new];
//    components.scheme = @"https";
//    components.host = originalComponents.host;
//    components.path = originalComponents.path;
//
//    NSURL *url = [components URL];

	[self play:stationUrl isReconnect:NO];
}

- (void)play:(NSURL*)stationUrl isReconnect:(bool)isReconnect
{
	NSError* error = nil;

	if ( (self.currentStationUrl != stationUrl) ||
		 (![self isPlaying] && (self.currentStationUrl == stationUrl)) )
	{
		// stop old player
		if ( self.audioPlayer )
		{
			[self.audioPlayerItem removeObserver:self forKeyPath:kPlayerStatus];
			[self.audioPlayerItem removeObserver:self forKeyPath:kPlayerTimedMetadata];
			[self.audioPlayerItem removeObserver:self forKeyPath:kPlayerBufferEmpty];
			[self.audioPlayerItem removeObserver:self forKeyPath:kPlayerLikelyToKeepUp];

			[self.audioPlayer pause];
			self.audioPlayer = nil;
			self.audioPlayerItem = nil;
		}

		if (!isReconnect)
		{
			self.reconnectCounter = 0;
		}

		self.currentStationUrl = stationUrl;
		self.audioPlayerItem = [AVPlayerItem playerItemWithURL:self.currentStationUrl];
		[self.audioPlayerItem addObserver:self forKeyPath:kPlayerStatus options:0 context:nil];
		[self.audioPlayerItem addObserver:self forKeyPath:kPlayerTimedMetadata options:NSKeyValueObservingOptionNew context:nil];
		[self.audioPlayerItem addObserver:self forKeyPath:kPlayerBufferEmpty options:NSKeyValueObservingOptionNew context:nil];
		[self.audioPlayerItem addObserver:self forKeyPath:kPlayerLikelyToKeepUp options:NSKeyValueObservingOptionNew context:nil];
//        [self.audioPlayerItem addObserver:self forKeyPath:@"loadedTimeRanges" options:NSKeyValueObservingOptionNew context:nil];

		[[NSNotificationCenter defaultCenter] addObserver:self
												 selector:@selector(playerItemFailedToPlayToEndTime:)
													 name:AVPlayerItemFailedToPlayToEndTimeNotification
												   object:self.audioPlayerItem];
		[[NSNotificationCenter defaultCenter] addObserver:self
												 selector:@selector(slowFadeOut)
													 name:@"QTRTimeKeeperTimerFired"
												   object:nil];

		self.audioPlayer = [AVPlayer playerWithPlayerItem:self.audioPlayerItem];
		self.audioPlayer.volume = 0.0f; // sets the item volume, not the system volume
		[self setIsInterupted:NO];

	}

	if ( self.audioPlayer == nil )
	{
//		[[LFDAnalytics sharedInstance] logError:[error description] forAction:@"self.audioPlayer.error" forViewNamed:NSStringFromClass([self class])];
		NSLog(@"%@", [error description]);
	}
	else if ( ![self isPlaying] )
	{
		[self addObservers];
		[self.audioPlayer pause];
	}
	else
	{
		[self.playerDelegate startedPlaying:self.audioPlayerItem];
	}

	[self.fadeTimer invalidate];
	self.fadeTimer = nil;

//    [NSTimer scheduledTimerWithTimeInterval:20.0 target:self selector:@selector(pause) userInfo:nil repeats:NO];
}

- (void)pause
{
	if ( self.audioPlayer )
	{
		[self.audioPlayer pause];
		[self.playerDelegate stoppedPlaying];


		if ( !self.isInterupted )
		{
			// force to create new audioPlayerItem, otherwise observers do not work
			self.currentStationUrl = nil;
			[self removeObservers];
		}
		[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];  //FIXME: not hiding it
	}
	[self.fadeTimer invalidate];
	self.fadeTimer = nil;

//	[[LFDAnalytics sharedInstance] logPlayingChanges:@"pause" label:@"" forViewNamed:NSStringFromClass([self class])];
}

- (void)tryReconncect
{
	self.reconnectCounter++;

	if ( self.reconnectCounter < kPlayerNumReconnects )
	{
		NSLog(@"tryReconncect %lu", (unsigned long)self.reconnectCounter);
//		[[LFDAnalytics sharedInstance] logPlayingChanges:@"tryReconncect" label:[NSString stringWithFormat:@"%lu", (unsigned long)self.reconnectCounter] forViewNamed:NSStringFromClass([self class])];
		[self play:self.currentStationUrl isReconnect:YES];
		self.reconnectTimer = [NSTimer scheduledTimerWithTimeInterval:5.0 target:self selector:@selector(tryReconncect) userInfo:nil repeats:NO];
	}
	else
	{
		NSLog(@"end tryReconncect %lu", (unsigned long)self.reconnectCounter);
//		[[LFDAnalytics sharedInstance] logPlayingChanges:@"end tryReconncect" label:[NSString stringWithFormat:@"%lu", (unsigned long)self.reconnectCounter] forViewNamed:NSStringFromClass([self class])];
		self.reconnectCounter = 0;
		[self pause];
	}

}

- (bool)isPlaying
{
	return (self.audioPlayer.rate > 0.0);
}

- (void)slowFadeOut
{
	if ( self.fadeTimer == nil )
	{
		[self fadePlayer:self.audioPlayer fromVolume:1.0 toVolume:0.0 overTime:kPlayerSlowFadeOutTime];
		self.fadeTimer = [NSTimer scheduledTimerWithTimeInterval:2.0 * kPlayerSlowFadeOutTime target:self selector:@selector(pause) userInfo:nil repeats:NO];
	}
}

- (void)quickFadeOut
{
	if ( self.fadeTimer == nil )
	{
		[self fadePlayer:self.audioPlayer fromVolume:1.0 toVolume:0.0 overTime:kPlayerQuickFadeOutTime];
		self.fadeTimer = [NSTimer scheduledTimerWithTimeInterval:2.0 * kPlayerQuickFadeOutTime target:self selector:@selector(pause) userInfo:nil repeats:NO];
	}
}

- (void)quickFadeIn
{
	if ( self.audioPlayer.volume < 0.5 ) // I assume then we are not playing
	{
		[self fadePlayer:self.audioPlayer fromVolume:0.0 toVolume:1.0 overTime:kPlayerQuickFadeInTime];
	}
}

- (void)fadePlayer:(AVPlayer*)player
		fromVolume:(float)startVolume
		  toVolume:(float)endVolume
		  overTime:(float)time
{

	// Update the volume every 1/40 of a second
	float fadeSteps = time * 40.0;

	self.audioPlayer.volume = startVolume;

	for ( int step = 0; step < fadeSteps; step++ )
	{
		double delayInSeconds = step * (time / fadeSteps);

		AVPlayer *weakPlayer = self.audioPlayer;

		dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
		dispatch_after(popTime, dispatch_get_main_queue(), ^(void) {

			float fraction = ((float)(step + 1) / fadeSteps);
			if ( weakPlayer.rate == 1.0 )
			{
				weakPlayer.volume = startVolume + (endVolume - startVolume) * fraction;
			}

			if ( weakPlayer.volume < 0.004 )
			{
				[weakPlayer pause];
//				[[LFDAnalytics sharedInstance] logPlayingChanges:@"fade out ended" label:@"pause" forViewNamed:NSStringFromClass([self class])];
			}
		});
	}
}

#pragma mark - KVO

- (void)addObservers
{
	// ensure we already have a singleton object
	[AVAudioSession sharedInstance];

	// register for notifications
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(interruption:)
												 name:AVAudioSessionInterruptionNotification
											   object:nil];

	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(routeChange:)
												 name:AVAudioSessionRouteChangeNotification
											   object:nil];
}

- (void)removeObservers
{
	[[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)interruption:(NSNotification*)notification
{
	// get the user info dictionary
	NSDictionary *interuptionDict = notification.userInfo;
	// get the AVAudioSessionInterruptionTypeKey enum from the dictionary
	NSInteger interuptionType = [[interuptionDict valueForKey:AVAudioSessionInterruptionTypeKey] integerValue];
	// decide what to do based on interruption type here...
	switch (interuptionType) {
		case AVAudioSessionInterruptionTypeBegan:
			NSLog(@"Audio Session Interruption case started.");
//			[[LFDAnalytics sharedInstance] logPlayingChanges:@"Audio Session Interruption" label:@"AVAudioSessionInterruptionTypeBegan" forViewNamed:NSStringFromClass([self class])];
			// fork to handling method here...
			// EG:[self handleInterruptionStarted];
			[self setIsInterupted:YES];
			break;

		case AVAudioSessionInterruptionTypeEnded:
			NSLog(@"Audio Session Interruption case ended.");
			// fork to handling method here...
			// EG:[self handleInterruptionEnded];
//			[[LFDAnalytics sharedInstance] logPlayingChanges:@"Audio Session Interruption" label:@"AVAudioSessionInterruptionTypeEnded" forViewNamed:NSStringFromClass([self class])];
			[self setIsInterupted:NO];
			[self play:self.currentStationUrl isReconnect:YES];
			break;

		default:
			NSLog(@"Audio Session Interruption Notification case default.");
			break;
	}
}

- (void)routeChange:(NSNotification*)notification
{

	NSDictionary *interuptionDict = notification.userInfo;

	NSInteger routeChangeReason = [[interuptionDict valueForKey:AVAudioSessionRouteChangeReasonKey] integerValue];

	switch (routeChangeReason) {
		case AVAudioSessionRouteChangeReasonUnknown:
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonUnknown");
			break;

		case AVAudioSessionRouteChangeReasonNewDeviceAvailable:
			// a headset was added or removed
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonNewDeviceAvailable");
			break;

		case AVAudioSessionRouteChangeReasonOldDeviceUnavailable:
			// a headset was added or removed
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonOldDeviceUnavailable");
			break;

		case AVAudioSessionRouteChangeReasonCategoryChange:
			// called at start - also when other audio wants to play
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonCategoryChange");//AVAudioSessionRouteChangeReasonCategoryChange
			break;

		case AVAudioSessionRouteChangeReasonOverride:
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonOverride");
			break;

		case AVAudioSessionRouteChangeReasonWakeFromSleep:
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonWakeFromSleep");
			break;

		case AVAudioSessionRouteChangeReasonNoSuitableRouteForCategory:
			NSLog(@"routeChangeReason : AVAudioSessionRouteChangeReasonNoSuitableRouteForCategory");
			break;

		default:
			break;
	}
}

#pragma mark - PlayerItem status

- (void)observeValueForKeyPath:(NSString*)keyPath ofObject:(id)object change:(NSDictionary*)change context:(void*)context
{
	NSLog(@"keyPath: %@", keyPath);

	if (object == self.audioPlayerItem && [keyPath isEqualToString:kPlayerStatus])
	{
		if (self.audioPlayerItem.status == AVPlayerStatusReadyToPlay)
		{
			NSLog(@"AVPlayerStatusReadyToPlay");
		}
		else if (self.audioPlayerItem.status == AVPlayerStatusFailed)
		{
			// something went wrong. player.error should contain some information
//			[[LFDAnalytics sharedInstance] logPlayingChanges:@"AVPlayerStatusFailed" label:[self.audioPlayerItem.error description] forViewNamed:NSStringFromClass([self class])];
			NSLog(@"AVPlayerStatusFailed");
			NSLog(@"%@", self.audioPlayerItem.error);

			[self.playerDelegate failedPlaying: self.audioPlayerItem.error];

		}
		else if (self.audioPlayerItem.status == AVPlayerItemStatusUnknown)
		{
//			[[LFDAnalytics sharedInstance] logPlayingChanges:@"audioPlayerItem.status" label:AVPlayerItemStatusUnknown forViewNamed:NSStringFromClass([self class])];
			NSLog(@"AVPlayerItemStatusUnknown");
		}
	}

	if ( [keyPath isEqualToString:kPlayerTimedMetadata] )
	{
		AVPlayerItem* playerItem = object;

		for ( AVMetadataItem* metadata in playerItem.timedMetadata )
		{
			NSLog(@"\nkey: %@\nkeySpace: %@\ncommonKey: %@\nvalue: %@\n", [metadata.key description], metadata.keySpace, metadata.commonKey, metadata.stringValue);

			if ( [metadata.commonKey isEqualToString:kPlayerTitle] )
			{
				[self.playerDelegate playingTitle:[metadata.value description]];
			}
		}

		if ( ([[change objectForKey:@"kind"] integerValue] == 1) &&
			([[change objectForKey:@"new"] isKindOfClass:[NSNull class]]) )
		{
//			[[LFDAnalytics sharedInstance] logPlayingChanges:@"new song" label:@"nil (stopped)" forViewNamed:NSStringFromClass([self class])];
			// stopped
			[self pause];
		}

	}

	if ( (object == self.audioPlayerItem) && [keyPath isEqualToString:kPlayerBufferEmpty] )
	{
		if (self.audioPlayerItem.playbackBufferEmpty && [self isPlaying])
		{
			if ( [[UIApplication sharedApplication] applicationState] == UIApplicationStateBackground )
			{
				// start background task so we can try to reconnect
				task = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^(void) {
				}];
				NSLog(@"beginBackgroundTask");
//				[[LFDAnalytics sharedInstance] logChanges:@"beginBackgroundTask" label:@"playbackBufferEmpty" inCategory:@"Playing" forViewNamed:NSStringFromClass([self class])];
			}
			NSLog(@"playbackBufferEmpty");
//			[[LFDAnalytics sharedInstance] logPlayingChanges:kPlayerBufferEmpty label:@"nil" forViewNamed:NSStringFromClass([self class])];
			[self.audioPlayer play];
		}
	}
	else if ( (object == self.audioPlayerItem) && [keyPath isEqualToString:kPlayerLikelyToKeepUp] )
	{
		if (self.audioPlayerItem.playbackLikelyToKeepUp)
		{

			NSLog(@"playbackLikelyToKeepUp");

			// this stops the reconnect
			[self.reconnectTimer invalidate];
			self.reconnectCounter = 0;

			[self.audioPlayer play];
			[self quickFadeIn];

			[self.playerDelegate startedPlaying:self.audioPlayerItem];

			if ( [[UIApplication sharedApplication] applicationState] == UIApplicationStateBackground )
			{
				// we are recconected, we don't need the background task anymore
				[[UIApplication sharedApplication] endBackgroundTask:task];
				task = 0;
				NSLog(@"endBackgroundTask");
//				[[LFDAnalytics sharedInstance] logChanges:@"endBackgroundTask" label:@"playbackLikelyToKeepUp" inCategory:@"Playing" forViewNamed:NSStringFromClass([self class])];
			}
//            [self.playerDelegate playingTitle:@""];
		}
	}
/*    else if ( (object == self.audioPlayerItem) && [keyPath isEqualToString:@"loadedTimeRanges"] )
	{
		NSArray *timeRanges = (NSArray *)[change objectForKey:NSKeyValueChangeNewKey];
		if (timeRanges.count > 0)
		{
			CMTimeRange timerange = [timeRanges[0] CMTimeRangeValue];

			CGFloat smartValue = CMTimeGetSeconds(CMTimeAdd(timerange.start, timerange.duration));
			CGFloat duration   = CMTimeGetSeconds(self.audioPlayer.currentTime);
			if (smartValue - duration > 5 && !self.isPlaying) {
				// Change the value "5" to your needed secs, its the buffer size.
				[self.audioPlayer play];
			}

			NSLog(@"laded time range: %f", smartValue - duration);
		}
	}*/
}

- (void) playerItemFailedToPlayToEndTime:(NSNotification*)notification
{
	NSError* error = notification.userInfo[AVPlayerItemFailedToPlayToEndTimeErrorKey];
	NSLog(@"playerItemFailedToPlayToEndTime");
//	[[LFDAnalytics sharedInstance] logPlayingChanges:@"playerItemFailedToPlayToEndTime" label:[error description] forViewNamed:NSStringFromClass([self class])];
	[self tryReconncect];

}

@end
