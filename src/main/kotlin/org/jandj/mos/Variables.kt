package org.jandj.mos

const val BlockSize:Int = 16
const val PageSize:Int = 16
const val PageCount:Int = 5 // 4 for VM's 1 for addressing and SuperVisor?
const val MEM_SIZE = BlockSize * PageSize * PageCount