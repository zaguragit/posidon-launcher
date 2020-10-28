package posidon.launcher.items

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools


class ContactItem private constructor(
    override var label: String?,
    val iconUri: Uri?,
    val lookupKey: String,
    val phone: String,
    val id: Int
) : LauncherItem() {

    init {
        if (iconUri == null) {
            icon = ThemeTools.generateContactPicture(label!!)
        }
    }

    fun open(context: Context) {
        val viewContact = Intent(Intent.ACTION_VIEW)
        viewContact.data = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
        viewContact.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        viewContact.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        context.startActivity(viewContact)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactItem

        if (id == other.id) return true
        if (lookupKey != other.lookupKey) return false
        if (phone != other.phone) return false
        if (label != other.label) return false
        if (iconUri != other.iconUri) return false

        return true
    }

    override fun hashCode() = id

    companion object {
        fun getList(requiresStar: Boolean = false): Iterable<ContactItem> {
            val cur = Tools.appContext!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.STARRED,
                        ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                        ContactsContract.Contacts.PHOTO_ID,
                        ContactsContract.Contacts._ID), null, null, null)

            val contactMap = HashMap<String, ContactItem>()

            if (cur != null) {

                val lookupIndex = cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                val displayNameIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val starredIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)
                val photoIdIndex = cur.getColumnIndex(ContactsContract.Contacts.PHOTO_ID)
                val contactIdIndex = cur.getColumnIndex(ContactsContract.Contacts._ID)

                if (cur.count != 0) {
                    while (cur.moveToNext()) {
                        val starred = cur.getInt(starredIndex) != 0
                        if (requiresStar && !starred) {
                            continue
                        }
                        val lookupKey = cur.getString(lookupIndex)
                        val name = cur.getString(displayNameIndex)
                        if (name.isNullOrBlank()) continue
                        val contactId = cur.getInt(contactIdIndex)
                        val phone = cur.getString(numberIndex) ?: ""
                        val photoId = cur.getString(photoIdIndex)
                        val icon: Uri? = if (photoId != null) {
                            ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId.toLong())
                        } else null

                        val contact = ContactItem(name, icon, lookupKey, phone, contactId)

                        if (!contactMap.containsKey(lookupKey)) {
                            contactMap[lookupKey] = contact
                        }
                    }
                }
                cur.close()
            }

            val nicknameCur = Tools.appContext!!.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Nickname.NAME, ContactsContract.Data.LOOKUP_KEY),
                ContactsContract.Data.MIMETYPE + "= ?",
                arrayOf(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE), null)

            if (nicknameCur != null) {
                if (nicknameCur.count != 0) {
                    val lookupKeyIndex = nicknameCur.getColumnIndex(ContactsContract.Data.LOOKUP_KEY)
                    val nickNameIndex = nicknameCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)
                    while (nicknameCur.moveToNext()) {
                        val lookupKey = nicknameCur.getString(lookupKeyIndex)
                        val nickname = nicknameCur.getString(nickNameIndex)
                        if (nickname != null && lookupKey != null && contactMap.containsKey(lookupKey)) {
                            contactMap[lookupKey]!!.label = nickname
                        }
                    }
                }
                nicknameCur.close()
            }

            return contactMap.values
        }
    }
}