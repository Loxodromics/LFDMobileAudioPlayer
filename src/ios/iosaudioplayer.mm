//
//  iosaudioplayer.mm
//  iOS Audio Player
//
//  Created by philipp on 27.09.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//

#include <MediaPlayer/MPMediaItem.h>
#include <MediaPlayer/MPMoviePlayerController.h>
#include <MediaPlayer/MPNowPlayingInfoCenter.h>
#include <MediaPlayer/MediaPlayer.h>
#include <MediaPlayer/MPRemoteCommandCenter.h>
#include "iosaudioplayer.h"
#include "LFDAudioPlayer.h"

@interface AudioPlayerDelegate : NSObject <LFDAudioPlayerDelegate> {
	LFD::IosAudioPlayer* m_iosAudioPlayer;
}
@end

@implementation AudioPlayerDelegate

- (id) initWithIosAudioPlayer:(LFD::IosAudioPlayer*)iosAudioPlayer
{
	NSLog(@"initWithIosAudioPlayer");
	self = [super init];
	if (self) {
		m_iosAudioPlayer = iosAudioPlayer;
		[[LFDAudioPlayer sharedInstance] setPlayerDelegate:self];

		// set that we want to play in background
		[[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];

		[self registerRemoteCommands];

//		self.connectingState = ConnectionStateNotConnecting;
	}
	return self;
}

- (void) registerRemoteCommands
{
	[self deregisterRemoteCommands];

	if ( !m_iosAudioPlayer->media() )
		return;

	[[[MPRemoteCommandCenter sharedCommandCenter] togglePlayPauseCommand] addTarget:self action:@selector(togglePlayPause:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] pauseCommand] addTarget:self action:@selector(pause:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] playCommand] addTarget:self action:@selector(togglePlayPause:)];

	if ( m_iosAudioPlayer->media()->hasNext() )
		[[[MPRemoteCommandCenter sharedCommandCenter] nextTrackCommand] addTarget:self action:@selector(nextTrack:)];
	if ( m_iosAudioPlayer->media()->hasPrevious() )
		[[[MPRemoteCommandCenter sharedCommandCenter] previousTrackCommand] addTarget:self action:@selector(previousTrack:)];
	if ( m_iosAudioPlayer->media()->isLikable() )
	{
		if ( m_iosAudioPlayer->media()->isLiked())
			[[[MPRemoteCommandCenter sharedCommandCenter] dislikeCommand] addTarget:self action:@selector(dislike:)];
		else
			[[[MPRemoteCommandCenter sharedCommandCenter] likeCommand] addTarget:self action:@selector(like:)];
	}
	if ( m_iosAudioPlayer->media()->canSeek() )
	{
		[[[MPRemoteCommandCenter sharedCommandCenter] seekForwardCommand] addTarget:self action:@selector(seekForward:)];
		[[[MPRemoteCommandCenter sharedCommandCenter] seekBackwardCommand] addTarget:self action:@selector(seekBackward:)];
	}
}

- (void) deregisterRemoteCommands
{
	[[[MPRemoteCommandCenter sharedCommandCenter] nextTrackCommand] removeTarget:self action:@selector(nextTrack:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] previousTrackCommand] removeTarget:self action:@selector(previousTrack:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] togglePlayPauseCommand] removeTarget:self action:@selector(togglePlayPause:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] pauseCommand] removeTarget:self action:@selector(pause:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] playCommand] removeTarget:self action:@selector(togglePlayPause:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] likeCommand] removeTarget:self action:@selector(like:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] dislikeCommand] removeTarget:self action:@selector(dislike:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] seekForwardCommand] removeTarget:self action:@selector(seekForward:)];
	[[[MPRemoteCommandCenter sharedCommandCenter] seekBackwardCommand] removeTarget:self action:@selector(seekBackward:)];
}

- (IBAction)nextTrack:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"nextTrack");
	self->m_iosAudioPlayer->nextTrackPressed();
}

- (IBAction)previousTrack:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"previousTrack");
	self->m_iosAudioPlayer->previousTrackPressed();
}

- (IBAction)togglePlayPause:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"togglePlayPause");
	self->m_iosAudioPlayer->togglePlayPause();
}

- (IBAction)pause:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"pause");
	self->m_iosAudioPlayer->pause();
}

- (IBAction)like:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"like");
	self->m_iosAudioPlayer->likePressed();
}

- (IBAction)dislike:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"dislike");
	self->m_iosAudioPlayer->dislikePressed();
}

- (IBAction)seekForward:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"seekForward");
	self->m_iosAudioPlayer->seekForwardPressed();
}

- (IBAction)seekBackward:(id)sender {
	Q_UNUSED(sender);
	NSLog(@"seekBackward");
	self->m_iosAudioPlayer->seekBackwardPressed();
}

#pragma mark - LFDAudioPlayerDelegate

- (void)startedPlaying:(AVPlayerItem*)playerItem
{
	Q_UNUSED(playerItem);
	NSLog(@"startedPlaying: ");
	[self setMediaProperties:nil];

	self->m_iosAudioPlayer->startedPlaying();
}

- (void)stoppedPlaying
{
	self->m_iosAudioPlayer->stoppedPlaying();
}

- (void)failedPlaying:(NSError*)error
{
	self->m_iosAudioPlayer->failedPlaying( QString::fromNSString([error description]) );
}

- (void)playingTitle:(NSString*)title
{
	NSLog(@"playingTitle: %@", title);
	[self setMediaProperties:title];
}

- (void)setMediaProperties:(NSString*)title
{
	NSLog(@"setMediaProperties %@", title);

	/// if we are playing something, update lock screen info
	if ( m_iosAudioPlayer->media() != nil )
	{
		NSMutableDictionary* songInfo = [[NSMutableDictionary alloc] init];

		if ( title )
		{
			[songInfo setObject:title forKey:MPMediaItemPropertyTitle];
			[songInfo setObject:m_iosAudioPlayer->media()->artist().toNSString() forKey:MPMediaItemPropertyArtist];
		}
		else
		{
			[songInfo setObject:m_iosAudioPlayer->media()->title().toNSString() forKey:MPMediaItemPropertyTitle];
			[songInfo setObject:m_iosAudioPlayer->media()->artist().toNSString() forKey:MPMediaItemPropertyArtist];
		}
		[songInfo setObject:m_iosAudioPlayer->media()->album().toNSString() forKey:MPMediaItemPropertyAlbumTitle];

		if ( !m_iosAudioPlayer->media()->localImageUrl().isEmpty() )
		{
			UIImage* albumArtImage = [UIImage imageWithContentsOfFile:m_iosAudioPlayer->media()->localImageUrl().toNSString()];
			MPMediaItemArtwork* albumArt = [[MPMediaItemArtwork alloc] initWithImage:albumArtImage];
			[songInfo setObject:albumArt forKey:MPMediaItemPropertyArtwork];
		}

		[[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:songInfo];
	}
}

@end

/*
 * IosAudioPlayer
 */

namespace LFD {

IosAudioPlayer::IosAudioPlayer(QObject* parent)
	: AudioPlayer(parent),
	  m_audioPlayerDelegate((__bridge_retained void*) [[AudioPlayerDelegate alloc] initWithIosAudioPlayer:this])
{
}

void IosAudioPlayer::play()
{
	NSLog(@"media path: %@", [NSURL URLWithString:this->media()->url().toNSString()]);
	if ( !this->media()->url().isEmpty() )
	{
		[[LFDAudioPlayer sharedInstance] play:[NSURL URLWithString:this->media()->url().toNSString()]];
		this->setPlayingState( PlayingState::Connecting );
	}
}

void IosAudioPlayer::pause()
{
	[[LFDAudioPlayer sharedInstance] pause];
	this->setPlayingState( PlayingState::Paused );
}

void IosAudioPlayer::startedPlaying()
{
	this->setPlayingState( PlayingState::Playing );
}

void IosAudioPlayer::stoppedPlaying()
{
	this->setPlayingState( PlayingState::NotConnected );
}

void IosAudioPlayer::failedPlaying(QString errorMessage)
{
	Q_UNUSED(errorMessage);

	this->setPlayingState( PlayingState::FailedPlaying );
}

void IosAudioPlayer::nextTrackPressed()
{
	emit nextTrack( this->mediaPath() );
}

void IosAudioPlayer::previousTrackPressed()
{
	emit previousTrack( this->mediaPath() );
}

void IosAudioPlayer::togglePlayPause()
{

	if ( [[LFDAudioPlayer sharedInstance] isPlaying] )
	{
		[[LFDAudioPlayer sharedInstance] pause];
	}
	else
	{
		if ( !this->media()->url().isEmpty() )
			[[LFDAudioPlayer sharedInstance] play:[NSURL URLWithString:this->media()->url().toNSString()]];
	}

	if ( ![[LFDAudioPlayer sharedInstance] isPlaying] )
	{
		this->setPlayingState( PlayingState::Paused );
	}
}

void IosAudioPlayer::likePressed()
{
	emit like( this->mediaPath() );
}

void IosAudioPlayer::dislikePressed()
{
	emit dislike( this->mediaPath() );
}

void IosAudioPlayer::seekForwardPressed()
{
	emit seekForward( this->mediaPath() );
}

void IosAudioPlayer::seekBackwardPressed()
{
	emit seekBackward( this->mediaPath() );
}

} /// namespace LFD
