package de.rwth.reversiai.net;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import de.rwth.reversiai.util.StateBuilder;
import org.junit.Assert;
import org.junit.Test;

public class PacketTest
{
    //Example Packets from networkSpecification.pdf
    @Test
    public void sendGroupPacket()
    {
        byte[] sengrouppacket = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x01, 0x17 };
        Packet sendgroup = new Packet( PacketType.GROUPNUMBER, (byte) 23 );
        assertArrayEquals( sengrouppacket, sendgroup.encode() );
        assertArrayEquals( new byte[] { 0x17 }, sendgroup.getData() );
        assertEquals( 23, sendgroup.getData()[ 0 ] );
        assertEquals( 1, sendgroup.getLength() );
        assertEquals( PacketType.GROUPNUMBER, sendgroup.getType() );
    }

    @Test
    public void mapPacket()
    {
        byte[] examplemappacket = new byte[] {
                0x02, 0x00, 0x00, 0x00, 0x34, 0x32, 0x0a, 0x30, 0x0a, 0x31, 0x20, 0x31, 0x0a, 0x32, 0x20, 0x36, 0x0a,
                0x2d, 0x20, 0x2d, 0x20, 0x62, 0x20, 0x78, 0x20, 0x32, 0x20, 0x2d, 0x0a, 0x2d, 0x20, 0x30, 0x20, 0x78,
                0x20, 0x31, 0x20, 0x2d, 0x20, 0x2d, 0x0a, 0x31, 0x20, 0x31, 0x20, 0x35, 0x20, 0x3c, 0x2d, 0x3e, 0x20,
                0x34, 0x20, 0x30, 0x20, 0x31, 0x0a
        };
        byte[] examplemapdata = new byte[] {
                0x32, 0x0a, 0x30, 0x0a, 0x31, 0x20, 0x31, 0x0a, 0x32, 0x20, 0x36, 0x0a, 0x2d, 0x20, 0x2d, 0x20, 0x62,
                0x20, 0x78, 0x20, 0x32, 0x20, 0x2d, 0x0a, 0x2d, 0x20, 0x30, 0x20, 0x78, 0x20, 0x31, 0x20, 0x2d, 0x20,
                0x2d, 0x0a, 0x31, 0x20, 0x31, 0x20, 0x35, 0x20, 0x3c, 0x2d, 0x3e, 0x20, 0x34, 0x20, 0x30, 0x20, 0x31,
                0x0a
        };
        String examplemapstring = "2\n0\n1 1\n2 6\n- - b x 2 -\n- 0 x 1 - -\n1 1 5 <-> 4 0 1\n";
        Packet recMap = new Packet( PacketType.MAP, examplemapdata );
        assertArrayEquals( examplemappacket, recMap.encode() );
        assertEquals( examplemapstring, recMap.dataToString() );
        assertEquals( 52, recMap.getLength() );
        assertEquals( PacketType.MAP, recMap.getType() );
        try
        {
            new StateBuilder().parseString( recMap.dataToString() );
        }
        catch ( Exception e )
        {
            Assert.assertFalse( e.toString() + "\n" + recMap.dataToString(), true );
        }
    }

    @Test
    public void playerNumberPacket()
    {
        byte[] exampleplayernpacket = new byte[] { 0x03, 0x00, 0x00, 0x00, 0x01, 0x02 };
        Packet playern = new Packet( PacketType.PLAYERNUMBER, (byte) 2 );
        assertArrayEquals( exampleplayernpacket, playern.encode() );
        assertArrayEquals( new byte[] { 0x02 }, playern.getData() );
        assertEquals( 1, playern.getLength() );
        assertEquals( PacketType.PLAYERNUMBER, playern.getType() );
    }

    @Test
    public void moveAnn1Packet()
    {
        byte[] examplep1moveannpacket = new byte[] { 0x06, 0x00, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01 };
        MoveAnnouncePacket p1moveann = new MoveAnnouncePacket( new byte[] { 0x00, 0x01, 0x00, 0x01, 0x00, 0x01 } );
        assertArrayEquals( examplep1moveannpacket, p1moveann.encode() );
        assertEquals( 1, p1moveann.getX() );
        assertEquals( 1, p1moveann.getY() );
        assertEquals( 0x01, p1moveann.getPlayer() );
        assertEquals( 0x00, p1moveann.getPreference() );
    }

    @Test
    public void moveReqPacket()
    {
        byte[] examplemovereqpacket =
                new byte[] { 0x04, 0x00, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xD4, (byte) 0xC0, 0x00 };
        MoveRequestPacket reqmove = new MoveRequestPacket( new byte[] { 0x00, 0x01, (byte) 0xd4, (byte) 0xc0, 0x00 } );
        assertArrayEquals( examplemovereqpacket, reqmove.encode() );
        assertEquals( 0, reqmove.getMaxDepth() );
        assertEquals( 120000, reqmove.getTimeLimit() );
    }

    @Test
    public void moveResp1Packet()
    {
        byte[] examplemoveresppacket = new byte[] { 0x05, 0x00, 0x00, 0x00, 0x05, 0x00, 0x02, 0x00, 0x00, (byte) 0x14 };
        MoveRespondPacket moveresp = new MoveRespondPacket( 2 ,0, (byte) 0x14 );
        assertArrayEquals( examplemoveresppacket, moveresp.encode() );
        assertEquals( 2, moveresp.getX() );
        assertEquals( 0, moveresp.getY() );
        assertEquals( (byte) 0x14, moveresp.getPreference() );
    }

    @Test
    public void moveAnn2Packet()
    {
        byte[] examplep2moveannpacket = new byte[] { 0x06, 0x00, 0x00, 0x00, 0x06, 0x00, 0x02, 0x00, 0x00, 0x14, 0x02 };
        MoveAnnouncePacket p2moveann = new MoveAnnouncePacket( new byte[] { 0x00, 0x02, 0x00, 0x00, 0x14, 0x02 } );
        assertArrayEquals( examplep2moveannpacket, p2moveann.encode() );
        assertEquals( 2, p2moveann.getX() );
        assertEquals( 0, p2moveann.getY() );
        assertEquals( 0x02, p2moveann.getPlayer() );
        assertEquals( 0x14, p2moveann.getPreference() );
    }

    @Test
    public void endPhase1Packet()
    {
        byte[] examplependofphase1packet = new byte[] { 0x08, 0x00, 0x00, 0x00, 0x00 };
        Packet endofphase1 = new Packet( PacketType.FIRSTPHASEEND, new byte[] {} );
        assertArrayEquals( examplependofphase1packet, endofphase1.encode() );
        assertEquals( 0, endofphase1.getLength() );
        assertEquals( PacketType.FIRSTPHASEEND, endofphase1.getType() );
    }

    @Test
    public void moveResp2Packet()
    {
        byte[] examplemoveresp2packet = new byte[] { 0x05, 0x00, 0x00, 0x00, 0x05, 0x00, 0x03, 0x00, 0x01, 0x00 };
        MoveRespondPacket moveresp2 = new MoveRespondPacket( 3,1 );
        assertArrayEquals( examplemoveresp2packet, moveresp2.encode() );
        assertEquals( 3, moveresp2.getX() );
        assertEquals( 1, moveresp2.getY() );
        assertEquals( 0x00, moveresp2.getPreference() );
    }

    @Test
    public void bombAnn1Packet()
    {
        byte[] examplep2bombannpacket = new byte[] { 0x06, 0x00, 0x00, 0x00, 0x06, 0x00, 0x03, 0x00, 0x01, 0x00, 0x02 };
        MoveAnnouncePacket p2bombann = new MoveAnnouncePacket( new byte[] { 0x00, 0x03, 0x00, 0x01, 0x00, 0x02 } );
        assertArrayEquals( examplep2bombannpacket, p2bombann.encode() );
        assertEquals( 3, p2bombann.getX() );
        assertEquals( 1, p2bombann.getY() );
        assertEquals( 0x02, p2bombann.getPlayer() );
        assertEquals( 0x00, p2bombann.getPreference() );
    }

    @Test
    public void endOfGamePacket()
    {
        byte[] examplependofgamepacket = new byte[] { 0x09, 0x00, 0x00, 0x00, 0x00 };
        Packet endofgame = new Packet( PacketType.ENDOFGAME, new byte[] {} );
        assertArrayEquals( examplependofgamepacket, endofgame.encode() );
        assertEquals( 0, endofgame.getLength() );
        assertEquals( PacketType.ENDOFGAME, endofgame.getType() );
    }

    @Test
    public void additionalRsp()
    {
        byte[] additionalrsppacket = new byte[] { 0x05, 0x00, 0x00, 0x00, 0x05, 0x01, 0x73, 0x08, 0x01, 0x00 };
        MoveRespondPacket moveresp = new MoveRespondPacket( 371, 2049, (byte) 0x00 );
        assertArrayEquals( additionalrsppacket, moveresp.encode() );
        assertEquals( 371, moveresp.getX() );
        assertEquals( 2049, moveresp.getY() );
        assertEquals( 0x00, moveresp.getPreference() );
    }
}