package com.bvmrs.bckch
package domain

case class ProtocolMessage(
                            version: Int,
                            sourceId: String,
                            destinationId: String,
                            errCode: String,
                            errMsg: String,
                            msgType: String,
                            crc: String,
                            body: String)

// Neighbors need to have a TTL until invalidation

trait PeerToPeerNetwork
// Neighbors are identified by address. Address is IP + port
case class Neighbor(id: String, address: String, pathId: String, invalidationTime: Long) extends PeerToPeerNetwork
//  case class Destination(id: String, route: List[])

case object ProtocolDelimiters {
  // Use JSON for PoC purposes
  final val GORGON_OVERLAY_NETWORK_PROTOCOL_FIELD_DELIMITER = "" //0x1F
  final val GORGON_OVERLAY_NETWORK_PROTOCOL_RECORD_DELIMITER = "" //0x1E
  final val GORGON_OVERLAY_NETWORK_PROTOCOL_MESSAGE_DELIMITER = "" //0x1D
}