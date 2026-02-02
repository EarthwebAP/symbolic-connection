package com.glyphos.symbolic.data

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Manager for reading contacts from the device's contact list
 * Uses Android's ContactsContract API
 */
class PhoneContactsManager(private val context: Context) {

    /**
     * Retrieves all contacts from the device's contact list
     * Requires READ_CONTACTS permission
     */
    suspend fun getPhoneContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()

        try {
            val contentResolver = context.contentResolver

            // Query all contacts
            val contactsCursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI
                ),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )

            contactsCursor?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    try {
                        val contactId = cursor.getString(idIndex)
                        val displayName = cursor.getString(nameIndex)

                        // Get phone numbers for this contact (inline to avoid nested withContext)
                        val phoneNumbers = mutableListOf<String>()
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )

                        phoneCursor?.use { pCursor ->
                            val numberIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            while (pCursor.moveToNext()) {
                                val phoneNumber = pCursor.getString(numberIndex)
                                if (!phoneNumber.isNullOrEmpty()) {
                                    phoneNumbers.add(phoneNumber)
                                }
                            }
                        }

                        // Create a Contact for each phone number
                        phoneNumbers.forEach { phoneNumber ->
                            val contact = Contact(
                                userId = UUID.randomUUID().toString(),
                                displayName = displayName,
                                phoneNumber = phoneNumber,
                                personalGlyph = null,
                                status = ContactStatus.ACTIVE,
                                addedAt = System.currentTimeMillis()
                            )
                            contacts.add(contact)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Continue with next contact
                    }
                }
            }
        } catch (e: SecurityException) {
            // Handle permission not granted gracefully
            return@withContext emptyList()
        } catch (e: Exception) {
            // Log error but continue
            e.printStackTrace()
        }

        contacts
    }



    /**
     * Searches for contacts by name
     */
    suspend fun searchPhoneContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()

        try {
            val contentResolver = context.contentResolver
            val selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?"
            val selectionArgs = arrayOf("%$query%")

            val contactsCursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
                ),
                selection,
                selectionArgs,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )

            contactsCursor?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    try {
                        val contactId = cursor.getString(idIndex)
                        val displayName = cursor.getString(nameIndex)

                        // Get phone numbers inline
                        val phoneNumbers = mutableListOf<String>()
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )

                        phoneCursor?.use { pCursor ->
                            val numberIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            while (pCursor.moveToNext()) {
                                val phoneNumber = pCursor.getString(numberIndex)
                                if (!phoneNumber.isNullOrEmpty()) {
                                    phoneNumbers.add(phoneNumber)
                                }
                            }
                        }

                        phoneNumbers.forEach { phoneNumber ->
                            val contact = Contact(
                                userId = UUID.randomUUID().toString(),
                                displayName = displayName,
                                phoneNumber = phoneNumber,
                                personalGlyph = null,
                                status = ContactStatus.ACTIVE,
                                addedAt = System.currentTimeMillis()
                            )
                            contacts.add(contact)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Continue with next contact
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contacts
    }
}
