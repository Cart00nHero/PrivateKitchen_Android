package com.cartoonhero.privatekitchen_android.actors.apolloApi

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.theatre.Actor
import com.cartoonhero.theatre.Scenario
import graphqlApollo.client.*
import graphqlApollo.client.type.*
import kotlinx.coroutines.*
import java.util.*

private val apolloSun: ApolloClient = ApolloClient.Builder()
    .serverUrl("http://54.95.141.126:8080/graphql/api")
    .build()

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Icarus(private val served: Scenario) : Actor() {
    private val apiScope = CoroutineScope(Dispatchers.Default)

    private fun actFindKitchen(
        uniqueId: String,
        complete:(ApiStatus, FindKitchenQuery.FindKitchen?) -> Unit
    ) {
        apiScope.launch {
            val query = FindKitchenQuery(uniqueId = uniqueId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findKitchen)
                    }
                }
            }
        }
    }
    private fun actFindKitchenMenu(
        kitchenId: String,
        complete: (ApiStatus, FindKitchenMenuQuery.FindKitchenMenu?) -> Unit
    ) {
        apiScope.launch {
            val query = FindKitchenMenuQuery(kitchenId = kitchenId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findKitchenMenu)
                    }
                }
            }
        }
    }
    private fun actFindMenu(
        kitchenId: String,
        complete: (ApiStatus, FindMenuQuery.FindMenu?) -> Unit
    ) {
        apiScope.launch {
            val query = FindMenuQuery(kitchenId = kitchenId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findMenu)
                    }
                }
            }
        }
    }
    private fun actFindUser(
        uniqueId: String,
        complete: (ApiStatus, FindUserQuery.FindUser?) -> Unit
    ) {
        apiScope.launch {
            val query = FindUserQuery(uniqueId = uniqueId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findUser)
                    }
                }
            }
        }
    }
    private fun actSearchMatchedAddress(
        queries: QueryAddress,
        complete: (ApiStatus, List<SearchMatchedAddressesQuery.SearchMatchedAddress?>?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchMatchedAddressesQuery(queries = queries)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, listOf())
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.searchMatchedAddresses)
                    }
                }
            }
        }
    }
    private fun actSearchMatchedKitchens(
        chefId: String, queries: QueryKitchen,
        complete: (ApiStatus, SearchMatchedKitchensQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchMatchedKitchensQuery(
                Optional.presentIfNotNull(chefId),
                queries
            )
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data)
                    }
                }
            }
        }
    }
    private fun actSearchMatchedUser(
        queries: QueryUser,
        complete: (ApiStatus, SearchMatchedUsersQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchMatchedUsersQuery(queries = queries)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data)
                    }
                }
            }
        }
    }
    private fun actSearchNearSubscribed(
        max: Coordinate,min: Coordinate,
        userId: String,
        complete: (ApiStatus, SearchNearSubscribedQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchNearSubscribedQuery(max, min, userId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data)
                    }
                }
            }
        }
    }
    private fun actSearchNearby(
        max: Coordinate, min: Coordinate,
        complete: (ApiStatus, SearchNearbyQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchNearbyQuery(max, min)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data)
                    }
                }
            }
        }
    }
    private fun actSearchOpenedNear(
        time: Date,
        max: Coordinate,min: Coordinate,
        complete: (ApiStatus, SearchOpenedNearQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchOpenedNearQuery(time, max, min)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data)
                    }
                }
            }
        }
    }
    private fun actPublishKitchen(
        chefId: String, kitchen: InputKitchen,
        complete: (ApiStatus, PublishKitchenMutation.PublishKitchen?) -> Unit
    ) {
        apiScope.launch {
            val mutation = PublishKitchenMutation(kitchen, chefId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(
                            ApiStatus.SUCCESS,
                            response.data?.publishKitchen
                        )
                    }
                }
            }
        }
    }
    private fun actSignUser(
        auth: InputAuth,user: InputUser,
        complete: (ApiStatus, SignUserMutation.SignUser?) -> Unit
    ) {
        apiScope.launch {
            val mutation = SignUserMutation(auth, user)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.signUser)
                    }
                }
            }
        }
    }
    private fun actSubscribeKitchen(
        kitchenId: String, userId: String,
        complete: (ApiStatus, SubscribeKitchenMutation.SubscribeKitchen?) -> Unit
    ) {
        apiScope.launch {
            val mutation = SubscribeKitchenMutation(kitchenId, userId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.subscribeKitchen)
                    }
                }
            }
        }
    }
    private fun actTransferKitchen(
        kitchenId: String, userId: String,
        complete: (ApiStatus, TransferKitchenMutation.TransferKitchen?) -> Unit
    ) {
        apiScope.launch {
            val mutation = TransferKitchenMutation(kitchenId, userId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.transferKitchen)
                    }
                }
            }
        }
    }
    private fun actUnsubscribeKitchen(
        kitchenId: String, userId: String,
        complete: (ApiStatus, UnsubscribeKitchenMutation.UnsubscribeKitchen?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UnsubscribeKitchenMutation(kitchenId, userId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.unsubscribeKitchen)
                    }
                }
            }
        }
    }
    private fun actUpdateInventory(
        kid: String, orders:List<ModifyItemAmount>,
        complete: (ApiStatus, UpdateInventoryMutation.UpdateInventory?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateInventoryMutation(orders, kid)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.updateInventory)
                    }
                }
            }
        }
    }
    private fun actUpdateKitchens(
        queries:QueryKitchen, modifies:ModifyKitchen,
        complete: (
            ApiStatus,
            List<UpdateMatchedKitchensMutation.UpdateMatchedKitchen?>?
        ) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateMatchedKitchensMutation(queries, modifies)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.updateMatchedKitchens)
                    }
                }
            }
        }
    }
    private fun actUpdateMenu(
        kitchenId: String, menu: ModifyMenu,
        complete: (ApiStatus, UpdateMenuMutation.UpdateMenu?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateMenuMutation(kitchenId, menu)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.updateMenu)
                    }
                }
            }
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    fun beFindKitchen(
        uniqueId: String,
        complete:(ApiStatus, FindKitchenQuery.FindKitchen?) -> Unit
    ) {
        tell { actFindKitchen(uniqueId, complete) }
    }
    fun beFindKitchenMenu(
        kitchenId: String,
        complete: (ApiStatus, FindKitchenMenuQuery.FindKitchenMenu?) -> Unit
    ) {
        tell { actFindKitchenMenu(kitchenId, complete) }
    }
    fun beFindMenu(
        kitchenId: String,
        complete: (ApiStatus, FindMenuQuery.FindMenu?) -> Unit
    ) {
        tell { actFindMenu(kitchenId, complete) }
    }
    fun beFindUser(
        uniqueId: String,
        complete: (ApiStatus, FindUserQuery.FindUser?) -> Unit
    ) {
        tell { actFindUser(uniqueId, complete) }
    }
    fun beSearchMatchedAddress(
        queries: QueryAddress,
        complete: (ApiStatus, List<SearchMatchedAddressesQuery.SearchMatchedAddress?>?) -> Unit
    ) {
        tell { actSearchMatchedAddress(queries, complete) }
    }
    fun beSearchMatchedKitchens(
        chefId: String, queries: QueryKitchen,
        complete: (ApiStatus, SearchMatchedKitchensQuery.Data?) -> Unit
    ) {
        tell { actSearchMatchedKitchens(chefId, queries, complete) }
    }
    fun beSearchMatchedUser(
        queries: QueryUser,
        complete: (ApiStatus, SearchMatchedUsersQuery.Data?) -> Unit
    ) {
        tell { actSearchMatchedUser(queries, complete) }
    }
    fun beSearchNearSubscribed(
        max: Coordinate,min: Coordinate,
        userId: String,
        complete: (ApiStatus, SearchNearSubscribedQuery.Data?) -> Unit
    ) {
        tell { actSearchNearSubscribed(max, min, userId, complete) }
    }
    fun beSearchNearby(
        max: Coordinate, min: Coordinate,
        complete: (ApiStatus, SearchNearbyQuery.Data?) -> Unit
    ) {
        tell { actSearchNearby(max, min, complete) }
    }
    fun beSearchOpenedNear(
        time: Date,
        max: Coordinate,min: Coordinate,
        complete: (ApiStatus, SearchOpenedNearQuery.Data?) -> Unit
    ) {
        tell { actSearchOpenedNear(time, max, min, complete) }
    }
    fun bePublishKitchen(
        chefId: String, kitchen: InputKitchen,
        complete: (ApiStatus, PublishKitchenMutation.PublishKitchen?) -> Unit
    ) {
        tell { actPublishKitchen(chefId, kitchen, complete) }
    }
    fun beSignUser(
        auth: InputAuth,user: InputUser,
        complete: (ApiStatus, SignUserMutation.SignUser?) -> Unit
    ) {
        tell { actSignUser(auth, user, complete) }
    }
    fun beSubscribeKitchen(
        kitchenId: String, userId: String,
        complete: (ApiStatus, SubscribeKitchenMutation.SubscribeKitchen?) -> Unit
    ) {
        tell { actSubscribeKitchen(kitchenId, userId, complete) }
    }
    fun beTransferKitchen(
        kitchenId: String, userId: String,
        complete: (ApiStatus, TransferKitchenMutation.TransferKitchen?) -> Unit
    ) {
        tell { actTransferKitchen(kitchenId, userId, complete) }
    }
    fun beUnsubscribeKitchen(
        kitchenId: String, userId: String,
        complete: (ApiStatus, UnsubscribeKitchenMutation.UnsubscribeKitchen?) -> Unit
    ) {
        tell { actUnsubscribeKitchen(kitchenId, userId, complete) }
    }
    fun beUpdateInventory(
        kid: String, orders:List<ModifyItemAmount>,
        complete: (ApiStatus, UpdateInventoryMutation.UpdateInventory?) -> Unit
    ) {
        tell { actUpdateInventory(kid, orders, complete) }
    }
    fun beUpdateKitchens(
        queries:QueryKitchen, modifies:ModifyKitchen,
        complete: (
            ApiStatus,
            List<UpdateMatchedKitchensMutation.UpdateMatchedKitchen?>?
        ) -> Unit
    ) {
        tell { actUpdateKitchens(queries, modifies, complete) }
    }
    fun beUpdateMenu(
        kitchenId: String, menu: ModifyMenu,
        complete: (ApiStatus, UpdateMenuMutation.UpdateMenu?) -> Unit
    ) {
        tell { actUpdateMenu(kitchenId, menu, complete) }
    }
}