webchannel
==========

a websocket &lt;-> redis pubsub bridge

* connect via websocket e.g., ws://localhost:9003

* webchannel will accept the following commands:


subscribe:

{"op": "subscribe", "channel": "channel-*"}


unsubscribe: 

{"op": "unsubscribe", "channel": "other-channel"}

or

{"op": "unsubscribe", "channel": "stuff-*"}


publish:

{"op": "publish", "channel": "channel-43", "message": "hi!"}


* when messages are sent to the redis topics you subscribe to, you will receive:

{"message":"hi!", "channel":"channel-43"}


* apache license

