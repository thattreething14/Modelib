package tree.modelib.api

import tree.modelib.blockbench.FileModelConverter
import tree.modelib.model.ModeledEntity
import tree.modelib.model.type.AnimationEntity
import tree.modelib.model.type.DynamicEntity
import tree.modelib.model.type.StaticEntity

object ModelLibrary {
    /**
     * Retrieves a list of all modeled entities.
     * This includes static entities, dynamic entities, and animation entities.
     */
    val allEntities: List<ModeledEntity>
        get() {
            val modeledEntities: MutableList<ModeledEntity> = ArrayList()
            modeledEntities.addAll(StaticEntity.staticEntities)
            modeledEntities.addAll(DynamicEntity.dynamicEntities)
            modeledEntities.addAll(AnimationEntity.animationEntities)
            return modeledEntities
        }
    fun allModels(): HashMap<String, FileModelConverter> {
        return FileModelConverter.convertedFileModels
    }
    /**
     * Checks if a model with the specified name exists.
     *
     * @param modelName The name of the model to check.
     * @return true if the model exists, false otherwise.
     */
    fun modelExists(modelName: String?): Boolean {
        return FileModelConverter.convertedFileModels.containsKey(modelName)
    }

    /**
     * Retrieves a list of dynamic entities.
     *
     * @return List of dynamic entities.
     */
    val dynamicEntities: List<DynamicEntity>
        get() = DynamicEntity.dynamicEntities

    /**
     * Retrieves a list of static entities.
     *
     * @return List of static entities.
     */
    val staticEntities: List<StaticEntity>
        get() = StaticEntity.staticEntities

    /**
     * Retrieves a list of animation entities.
     *
     * @return List of animation entities.
     */
    val animationEntities: List<AnimationEntity>
        get() = AnimationEntity.animationEntities

}