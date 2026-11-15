package ar.edu.unlam.tuiter.data.repository

import ar.edu.unlam.tuiter.data.local.DraftDao
import ar.edu.unlam.tuiter.data.local.DraftEntity

// Repositorio para el borrador local.
class DraftRepository(private val draftDao: DraftDao) {

    suspend fun getDraft(): String? {
        return draftDao.getDraft()?.message
    }

    suspend fun saveDraft(message: String) {
        draftDao.saveDraft(DraftEntity(id = 1, message = message))
    }

    suspend fun clearDraft() {
        draftDao.clearDraft()
    }
}