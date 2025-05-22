@file:OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)

package org.jandj.mos.machine

class Word(num: UInt) {
    var bytes: UInt = num
    constructor(bytes: CharArray): this(){
        this.bytes = this.toUInt(UByteArray(4) { i -> bytes[i].code.toUByte() })
    }
    constructor() : this(0u)


    operator fun set(idx: Int, byte: UByte) {
        val temp:UByteArray = toByteArray(bytes)
        temp[idx] = byte
        bytes = toUInt(temp)
    }
    operator fun get(idx: Int): UInt {
        val significant = 0xFF000000u.shr(idx*8)
        return bytes.and(significant).shr(24 -idx*8)
    }
    fun toByteArray(num: UInt): UByteArray {
        val word = UByteArray(4)
        word[0] = num.and("FF000000".hexToUInt()).shr(24).toUByte()
        word[1] = num.and("00FF0000".hexToUInt()).shr(16).toUByte()
        word[2] = num.and("0000FF00".hexToUInt()).shr(8).toUByte()
        word[3] = num.and("000000FF".hexToUInt()).toUByte()
        return word
    }
    fun toArray(): ByteArray {
        val word = ByteArray(4)
        word[0] = bytes.and("FF000000".hexToUInt()).shr(24).toByte()
        word[1] = bytes.and("00FF0000".hexToUInt()).shr(16).toByte()
        word[2] = bytes.and("0000FF00".hexToUInt()).shr(8).toByte()
        word[3] = bytes.and("000000FF".hexToUInt()).toByte()
        return word
    }
    fun toCharArray(): CharArray {
        val word = CharArray(4)
        word[0] = Char(bytes.and("FF000000".hexToUInt()).shr(24).toInt())
        word[1] = Char(bytes.and("00FF0000".hexToUInt()).shr(16).toInt())
        word[2] = Char(bytes.and("0000FF00".hexToUInt()).shr(8).toInt())
        word[3] = Char(bytes.and("000000FF".hexToUInt()).toInt())
        return word
    }
    override fun toString(): String {
        return String(this.toCharArray())
    }
    fun toUInt(word:UByteArray): UInt{
        return (word[0].toUInt().shl(24) + (word[1].toUInt().shl(16)) + (word[2].toUInt().shl(8)) + word[3].toUInt())
    }
    operator fun plus(bx: Word): Word {
        return Word(this.bytes + bx.bytes)
    }
    operator fun minus(bx: Word): Word {
        return Word(this.bytes - bx.bytes)
    }
    operator fun times(bx: Word): Word {
        return Word(this.bytes * bx.bytes)
    }
    operator fun div(bx: Word): Word {
        return Word(this.bytes.div(bx.bytes))
    }
    operator fun inc(): Word {
        return Word(this.bytes + 1u)
    }
    operator fun dec(): Word {
        return Word(this.bytes - 1u)
    }
    operator fun rem(bx: Word): Word {
        return Word(this.bytes % bx.bytes)
    }
    operator fun compareTo(i: Word): Int {
        return this.bytes.compareTo(i.bytes)
    }
}