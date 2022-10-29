import asyncio
import websockets
 
async def test():
    async with websockets.connect('wss://127.0.0.1:8080/api/sse/register/abcdefghijkmlnop123') as websocket:
        await websocket.send("hello")
        response = await websocket.recv()
        print(response)
 
asyncio.get_event_loop().run_until_complete(test())