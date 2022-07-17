package com.cartoonhero.privatekitchen_android.actors.apolloApi

import com.apollographql.apollo3.ApolloClient
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.TimeGuardian
import com.cartoonhero.privatekitchen_android.props.entities.OrderTimeFrame
import com.cartoonhero.theatre.Actor
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.*
import graphqlApollo.operation.type.*
import kotlinx.coroutines.*
import java.util.*

private val apolloSun: ApolloClient = ApolloClient.Builder()
    .serverUrl("http://54.95.141.126:8080/graphql/api")
    .build()

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Helios(private val served: Scenario) : Actor() {
    private val apiScope = CoroutineScope(Dispatchers.Default)

    private fun actFindWorkstation(
        uniqueId: String,
        complete: (ApiStatus, FindWorkstationQuery.FindWorkstation?) -> Unit
    ) {
        apiScope.launch {
            val query = FindWorkstationQuery(uniqueId = uniqueId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findWorkstation)
                    }
                }
            }
        }
    }

    private fun actSearchMatchedWorkstations(
        queries: QueryWorkstation,
        complete: (
            ApiStatus,
            List<SearchMatchedWorkstationsQuery.SearchMatchedWorkstation?>?
        ) -> Unit
    ) {
        apiScope.launch {
            val query = SearchMatchedWorkstationsQuery(queries = queries)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(
                            ApiStatus.SUCCESS,
                            response.data?.searchMatchedWorkstations
                        )
                    }
                }
            }
        }
    }

    private fun actCreateWorkstation(
        workstation: InputWorkstation,
        complete: (ApiStatus, CreateWorkstationMutation.CreateWorkstation?) -> Unit
    ) {
        apiScope.launch {
            val mutation = CreateWorkstationMutation(workstation = workstation)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.createWorkstation)
                    }
                }
            }
        }
    }

    private fun actDeleteMatchedWorkstations(
        queries: QueryWorkstation,
        complete: (
            ApiStatus, List<DeleteMatchedWorkstationsMutation.DeleteMatchedWorkstation?>?
        ) -> Unit
    ) {
        apiScope.launch {
            val mutation = DeleteMatchedWorkstationsMutation(queries = queries)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.deleteMatchedWorkstations)
                    }
                }
            }
        }
    }

    private fun actDeleteWorkstation(
        uniqueId: String,
        complete: (ApiStatus, DeleteWorkstationMutation.DeleteWorkstation?) -> Unit
    ) {
        apiScope.launch {
            val mutation = DeleteWorkstationMutation(uniqueId = uniqueId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.deleteWorkstation)
                    }
                }
            }
        }
    }

    private fun actSaveWorkstationKitchen(
        uniqueId: String, kitchen: InputStKitchen,
        complete: (ApiStatus, SaveWorkstationKitchenMutation.SaveWorkstationKitchen?) -> Unit
    ) {
        apiScope.launch {
            val mutation = SaveWorkstationKitchenMutation(kitchen, uniqueId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.saveWorkstationKitchen)
                    }
                }
            }
        }
    }

    private fun actUpdateWorkstation(
        uniqueId: String, workstation: ModifyWorkstation,
        complete: (ApiStatus, UpdateWorkstationMutation.UpdateWorkstation?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateWorkstationMutation(uniqueId, workstation)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.updateWorkstation)
                    }
                }
            }
        }
    }

    // MARK: - Dashboard
    private fun actFindDashboard(
        stationId: String,
        complete: (ApiStatus, FindDashboardQuery.FindDashboard?) -> Unit
    ) {
        apiScope.launch {
            val query = FindDashboardQuery(stationId = stationId)
            val response = apolloSun.query(query).execute()
            served.launch {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findDashboard)
                    }
                }
            }
        }
    }

    private fun actUpdateDashboard(
        stationId: String, dashboard: InputDashboard,
        complete: (ApiStatus, UpdateDashboardMutation.UpdateDashboard?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateDashboardMutation(dashboard, stationId)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.updateDashboard)
                    }
                }
            }
        }
    }

    // MARK: - Storage
    private fun actFindStorehouse(
        stationId: String,
        complete: (ApiStatus, FindStorehouseQuery.FindStorehouse?) -> Unit
    ) {
        apiScope.launch {
            val query = FindStorehouseQuery(stationId = stationId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findStorehouse)
                    }
                }
            }
        }
    }

    private fun actGetDiningWays(
        kitchenId: String,
        complete: (ApiStatus, GetDiningWaysQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = GetDiningWaysQuery(kitchenId = kitchenId)
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

    private fun actUpdateStorehouse(
        stationId: String, storage: InputStorehouse,
        complete: (ApiStatus, UpdateStorehouseMutation.UpdateStorehouse?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateStorehouseMutation(stationId, storage)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.updateStorehouse)
                    }
                }
            }
        }
    }

    // MARK: - Order
    private fun actFindOrderForm(
        uniqueId: String,
        complete: (ApiStatus, FindOrderFormQuery.FindOrderForm?) -> Unit
    ) {
        apiScope.launch {
            val query = FindOrderFormQuery(uniqueId = uniqueId)
            val response = apolloSun.query(query).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.findOrderForm)
                    }
                }
            }
        }
    }

    private fun actSearchMatchedOrders(
        queries: QueryOrder,
        complete: (ApiStatus, SearchMatchedOrdersQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchMatchedOrdersQuery(queries = queries)
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

    private fun actSearchTimeFrameOrders(
        startDate: Date?, endDate: Date?,
        complete: (ApiStatus, SearchTimeFrameOrdersQuery.Data?) -> Unit
    ) {
        if (startDate == null && endDate == null) return
        apiScope.launch {
            val params = getTimeFrame(startDate, endDate)
            val query = Transformer().beMapToEntity<String, SearchTimeFrameOrdersQuery>(
                params
            )
            if (query != null) {
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
    }

    private fun actSearchMatchTimeOrders(
        startDate: Date?, endDate: Date?,
        queries: QueryOrder,
        complete: (ApiStatus, List<SearchMatchTimeOrdersQuery.SearchMatchTimeOrder?>?) -> Unit
    ) {
        if (startDate == null && endDate == null) return
        apiScope.launch {
            val params = getTimeFrame(startDate, endDate)
            val timeFrame = Transformer().beMapToEntity<String, OrderTimeFrame>(
                params
            )
            if (timeFrame != null) {
                timeFrame.queries = queries
                val query = Transformer().beTransfer<OrderTimeFrame, SearchMatchTimeOrdersQuery>(
                    timeFrame
                )
                if (query != null) {
                    val response = apolloSun.query(query).execute()
                    served.tell {
                        when {
                            response.hasErrors() -> {
                                complete(ApiStatus.FAILED, null)
                            }
                            else -> {
                                complete(ApiStatus.SUCCESS, response.data?.searchMatchTimeOrders)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun actSearchArrivalFrameOrders(
        period: TimePeriod,
        complete: (ApiStatus, SearchArrivalFrameOrdersQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchArrivalFrameOrdersQuery(
                period = period
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

    private fun actSearchMatchArrivalOrders(
        period: TimePeriod, queries: QueryOrder,
        complete: (ApiStatus, SearchMatchArrivalOrdersQuery.Data?) -> Unit
    ) {
        apiScope.launch {
            val query = SearchMatchArrivalOrdersQuery(
                period, queries
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

    private fun actCreateOrderForm(
        order: InputOrderForm,
        complete: (ApiStatus, CreateOrderFormMutation.CreateOrderForm?) -> Unit
    ) {
        apiScope.launch {
            val mutation = CreateOrderFormMutation(order = order)
            val response = apolloSun.mutation(mutation).execute()
            served.tell {
                when {
                    response.hasErrors() -> {
                        complete(ApiStatus.FAILED, null)
                    }
                    else -> {
                        complete(ApiStatus.SUCCESS, response.data?.createOrderForm)
                    }
                }
            }
        }
    }

    private fun actDeleteMatchTimeForms(
        startDate: Date?, endDate: Date?,
        queries: QueryOrder,
        complete: (ApiStatus, DeleteMatchTimeFormsMutation.Data?) -> Unit
    ) {
        if (startDate == null && endDate == null) return
        apiScope.launch {
            val params = getTimeFrame(startDate, endDate)
            val timeFrame = Transformer().beMapToEntity<String, OrderTimeFrame>(
                params
            )
            if (timeFrame != null) {
                timeFrame.queries = queries
                val mutation = Transformer()
                    .beTransfer<OrderTimeFrame, DeleteMatchTimeFormsMutation>(
                        timeFrame
                    )
                if (mutation != null) {
                    val response = apolloSun.mutation(mutation).execute()
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
        }
    }

    private fun actDeleteMatchedForms(
        queries: QueryOrder,
        complete: (ApiStatus, DeleteMatchedFormsMutation.Data?) -> Unit
    ) {
        apiScope.launch {
            val mutation = DeleteMatchedFormsMutation(
                queries = queries
            )
            val response = apolloSun.mutation(mutation).execute()
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

    private fun actDeleteTimeFrameForms(
        startDate: Date?, endDate: Date?,
        complete: (ApiStatus, DeleteTimeFrameFormsMutation.Data?) -> Unit
    ) {
        apiScope.launch {
            val params = getTimeFrame(startDate, endDate)
            val mutation = Transformer()
                .beMapToEntity<String, DeleteTimeFrameFormsMutation>(
                    params
                )
            if (mutation != null) {
                val response = apolloSun.mutation(mutation).execute()
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
    }

    private fun actUpdateMatchTimeForms(
        startDate: Date?, endDate: Date?,
        modifies: ModifyOrder, queries: QueryOrder,
        complete: (ApiStatus, UpdateMatchTimeFormsMutation.Data?) -> Unit
    ) {
        apiScope.launch {
            val params = getTimeFrame(startDate, endDate)
            val timeFrame = Transformer().beMapToEntity<String, OrderTimeFrame>(
                params
            )
            if (timeFrame != null) {
                timeFrame.modifies = modifies
                timeFrame.queries = queries
                val mutation = Transformer()
                    .beTransfer<OrderTimeFrame, UpdateMatchTimeFormsMutation>(
                        timeFrame
                    )
                if (mutation != null) {
                    val response = apolloSun.mutation(mutation).execute()
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
        }
    }

    private fun actUpdateMatchedForms(
        modifies: ModifyOrder, queries: QueryOrder,
        complete: (ApiStatus, UpdateMatchedFormsMutation.Data?) -> Unit
    ) {
        apiScope.launch {
            val mutation = UpdateMatchedFormsMutation(modifies, queries)
            val response = apolloSun.mutation(mutation).execute()
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

    private fun actUpdateTimeFrameForms(
        startDate: Date?, endDate: Date?,
        complete: (ApiStatus, UpdateTimeFrameFormsMutation.Data?) -> Unit
    ) {
        apiScope.launch {
            val params = getTimeFrame(startDate, endDate)
            val mutation = Transformer()
                .beMapToEntity<String, UpdateTimeFrameFormsMutation>(
                    params
                )
            if (mutation != null) {
                val response = apolloSun.mutation(mutation).execute()
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
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private suspend fun getTimeFrame(startDate: Date?, endDate: Date?): Map<String, String> {
        val params: MutableMap<String, String> = mutableMapOf()
        if (startDate != null) {
            params["startDate"] = TimeGuardian().beISO8601(startDate)
        }
        if (endDate != null) {
            params["endDate"] = TimeGuardian().beISO8601(endDate)
        }
        return params.toMap()
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    fun beFindWorkstation(
        uniqueId: String,
        complete: (ApiStatus, FindWorkstationQuery.FindWorkstation?) -> Unit
    ) {
        tell { actFindWorkstation(uniqueId, complete) }
    }

    fun beSearchMatchedWorkstations(
        queries: QueryWorkstation,
        complete: (
            ApiStatus, List<SearchMatchedWorkstationsQuery.SearchMatchedWorkstation?>?
        ) -> Unit
    ) {
        tell { actSearchMatchedWorkstations(queries, complete) }
    }

    fun beCreateWorkstation(
        workstation: InputWorkstation,
        complete: (ApiStatus, CreateWorkstationMutation.CreateWorkstation?) -> Unit
    ) {
        tell { actCreateWorkstation(workstation, complete) }
    }

    fun beDeleteMatchedWorkstations(
        queries: QueryWorkstation,
        complete: (
            ApiStatus, List<DeleteMatchedWorkstationsMutation.DeleteMatchedWorkstation?>?
        ) -> Unit
    ) {
        tell { actDeleteMatchedWorkstations(queries, complete) }
    }

    fun beDeleteWorkstation(
        uniqueId: String,
        complete: (ApiStatus, DeleteWorkstationMutation.DeleteWorkstation?) -> Unit
    ) {
        tell { actDeleteWorkstation(uniqueId, complete) }
    }

    fun beSaveWorkstationKitchen(
        uniqueId: String, kitchen: InputStKitchen,
        complete: (ApiStatus, SaveWorkstationKitchenMutation.SaveWorkstationKitchen?) -> Unit
    ) {
        tell { actSaveWorkstationKitchen(uniqueId, kitchen, complete) }
    }

    fun beUpdateWorkstation(
        uniqueId: String, workstation: ModifyWorkstation,
        complete: (ApiStatus, UpdateWorkstationMutation.UpdateWorkstation?) -> Unit
    ) {
        tell { actUpdateWorkstation(uniqueId, workstation, complete) }
    }

    fun beFindDashboard(
        stationId: String,
        complete: (ApiStatus, FindDashboardQuery.FindDashboard?) -> Unit
    ) {
        tell { actFindDashboard(stationId, complete) }
    }

    fun beUpdateDashboard(
        stationId: String, dashboard: InputDashboard,
        complete: (ApiStatus, UpdateDashboardMutation.UpdateDashboard?) -> Unit
    ) {
        tell { actUpdateDashboard(stationId, dashboard, complete) }
    }

    fun beFindStorehouse(
        stationId: String,
        complete: (ApiStatus, FindStorehouseQuery.FindStorehouse?) -> Unit
    ) {
        tell { actFindStorehouse(stationId, complete) }
    }

    fun beGetDiningWays(
        kitchenId: String,
        complete: (ApiStatus, GetDiningWaysQuery.Data?) -> Unit
    ) {
        tell { actGetDiningWays(kitchenId, complete) }
    }

    fun beUpdateStorehouse(
        stationId: String, storage: InputStorehouse,
        complete: (ApiStatus, UpdateStorehouseMutation.UpdateStorehouse?) -> Unit
    ) {
        tell { actUpdateStorehouse(stationId, storage, complete) }
    }

    fun beFindOrderForm(
        uniqueId: String,
        complete: (ApiStatus, FindOrderFormQuery.FindOrderForm?) -> Unit
    ) {
        tell { actFindOrderForm(uniqueId, complete) }
    }

    fun beSearchMatchedOrders(
        queries: QueryOrder,
        complete: (ApiStatus, SearchMatchedOrdersQuery.Data?) -> Unit
    ) {
        tell { actSearchMatchedOrders(queries, complete) }
    }

    fun beSearchTimeFrameOrders(
        startDate: Date?, endDate: Date?,
        complete: (ApiStatus, SearchTimeFrameOrdersQuery.Data?) -> Unit
    ) {
        tell { actSearchTimeFrameOrders(startDate, endDate, complete) }
    }

    fun beSearchMatchTimeOrders(
        startDate: Date?, endDate: Date?,
        queries: QueryOrder,
        complete: (ApiStatus, List<SearchMatchTimeOrdersQuery.SearchMatchTimeOrder?>?) -> Unit
    ) {
        tell { actSearchMatchTimeOrders(startDate, endDate, queries, complete) }
    }

    fun beSearchArrivalFrameOrders(
        period: TimePeriod,
        complete: (ApiStatus, SearchArrivalFrameOrdersQuery.Data?) -> Unit
    ) {
        tell { actSearchArrivalFrameOrders(period, complete) }
    }

    fun beSearchMatchArrivalOrders(
        period: TimePeriod, queries: QueryOrder,
        complete: (ApiStatus, SearchMatchArrivalOrdersQuery.Data?) -> Unit
    ) {
        tell { actSearchMatchArrivalOrders(period, queries, complete) }
    }

    fun beCreateOrderForm(
        order: InputOrderForm,
        complete: (ApiStatus, CreateOrderFormMutation.CreateOrderForm?) -> Unit
    ) {
        tell { actCreateOrderForm(order, complete) }
    }

    fun beDeleteMatchTimeForms(
        startDate: Date?, endDate: Date?,
        queries: QueryOrder,
        complete: (ApiStatus, DeleteMatchTimeFormsMutation.Data?) -> Unit
    ) {
        tell { actDeleteMatchTimeForms(startDate, endDate, queries, complete) }
    }

    fun beDeleteMatchedForms(
        queries: QueryOrder,
        complete: (ApiStatus, DeleteMatchedFormsMutation.Data?) -> Unit
    ) {
        tell { actDeleteMatchedForms(queries, complete) }
    }

    fun beDeleteTimeFrameForms(
        startDate: Date?, endDate: Date?,
        complete: (ApiStatus, DeleteTimeFrameFormsMutation.Data?) -> Unit
    ) {
        tell { actDeleteTimeFrameForms(startDate, endDate, complete) }
    }

    fun beUpdateMatchTimeForms(
        startDate: Date?, endDate: Date?,
        modifies: ModifyOrder, queries: QueryOrder,
        complete: (ApiStatus, UpdateMatchTimeFormsMutation.Data?) -> Unit
    ) {
        tell { actUpdateMatchTimeForms(startDate, endDate, modifies, queries, complete) }
    }

    fun beUpdateMatchedForms(
        modifies: ModifyOrder, queries: QueryOrder,
        complete: (ApiStatus, UpdateMatchedFormsMutation.Data?) -> Unit
    ) {
        tell { actUpdateMatchedForms(modifies, queries, complete) }
    }

    fun beUpdateTimeFrameForms(
        startDate: Date?, endDate: Date?,
        complete: (ApiStatus, UpdateTimeFrameFormsMutation.Data?) -> Unit
    ) {
        tell { actUpdateTimeFrameForms(startDate, endDate, complete) }
    }
}