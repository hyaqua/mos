package org.jandj.mos.os

enum class ResourceStatus {
    CREATED, IN_USE, RELEASED, DESTROYED
}

class Resource (
    intId: Int, extId: ResName,
    os: OS, creator: JProcess, reusable: Boolean,
    var component: Any?
) {
    var status: ResourceStatus = ResourceStatus.CREATED
    var resDesc: ResourceDescriptor = ResourceDescriptor(intId, extId, reusable, os, component, creator)

    override fun toString(): String{
        var out = "${resDesc.intId} : ${resDesc.extId}"
        if(resDesc.user != null){
            out += " [User: ${resDesc.user!!.pDesc.extID}]"
        }
        if(component != null){
            out += " [comp: ${component!!::class.simpleName}]"
        } else{
            out += " [comp: null]"
        }
        return out
    }
}
