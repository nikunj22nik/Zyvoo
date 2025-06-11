package com.business.zyvo.remote

import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.model.host.PropertyDetailsSave
import com.google.android.gms.common.annotation.KeepForSdkWithMembers
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST

import retrofit2.http.PUT

import retrofit2.http.Part
import retrofit2.http.Path
import java.nio.file.DirectoryStream.Filter


interface ZyvoApi {


    @POST("signup_phone_number")
    @FormUrlEncoded
    suspend fun signUpPhoneNumber(@Field("phone_number")phoneNumber :String,
                                  @Field("country_code")countryCode : String) : Response<JsonObject>

    @POST("login_phone_number")
    @FormUrlEncoded
    suspend fun loginPhoneNumber(@Field("phone_number")phoneNumber :String,
                                 @Field("country_code")countryCode : String,
                                 @Field("fcm_token")fcmToken:String,
                                 @Field("device_type")deviceType:String) : Response<JsonObject>

    @POST("otp_verify_login_phone")
    @FormUrlEncoded
    suspend fun otpVerifyLoginPhone(@Field("user_id")user_id :String,
                                    @Field("otp")otp : String,
                                    @Field("fcm_token")fcmToken :String?,
                                    @Field("device_type") deviceType:String) : Response<JsonObject>

    @POST("otp_verify_signup_phone")
    @FormUrlEncoded
    suspend fun otpVerifySignupPhone(@Field("temp_id")temp_id :String,
                                     @Field("otp")otp : String,
                                     @Field("fcm_token")fcmToken :String,
                                     @Field("device_type") deviceType:String) : Response<JsonObject>

    @POST("login_email")
    @FormUrlEncoded
    suspend fun loginEmail(@Field("email")email :String,
                           @Field("password")password : String,
                           @Field("fcm_token")fcmToken :String,
                           @Field("device_type")deviceType: String) : Response<JsonObject>


    @POST("signup_email")
    @FormUrlEncoded
    suspend fun signupEmail(@Field("email")email :String,
                           @Field("password")password : String) : Response<JsonObject>

    @POST("otp_verify_signup_email")
    @FormUrlEncoded
    suspend fun otpVerifySignupEmail(@Field("temp_id")temp_id :String,
                                     @Field("otp")otp : String,
                                     @Field("fcm_token")fcmToken :String,
                                     @Field("device_type") deviceType:String) : Response<JsonObject>


    @POST("forgot_password")
    @FormUrlEncoded
    suspend fun forgotPassword(@Field("email")email :String) : Response<JsonObject>

    @POST("otp_verify_forgot_password")
    @FormUrlEncoded
    suspend fun otpVerifyForgotPassword(@Field("user_id")user_id :String,
                                        @Field("otp")otp :String) : Response<JsonObject>


    @POST("reset_password")
    @FormUrlEncoded
    suspend fun resetPassword(@Field("user_id")user_id :String,
                              @Field("password")password :String,
                              @Field("password_confirmation")password_confirmation :String) : Response<JsonObject>

    @POST("get_user_profile")
    @FormUrlEncoded
    suspend fun getUserProfile(@Field("user_id")user_id :String) : Response<JsonObject>

    @POST("store_property_details")
    suspend fun addProperty(@Body addProperty : PropertyDetailsSave) : Response<JsonObject>

    @POST("get_properties_lists")
    @FormUrlEncoded
    suspend fun getMyPlacesApi(@Field("user_id")user_id : Int,@Field("latitude")latitude :Double?,@Field("longitude")lng:Double?) : Response<JsonObject>

    @POST("get_properties_lists")
    @FormUrlEncoded
    suspend fun getMyPlacesWithOutLatLangApi(@Field("user_id")user_id : Int) : Response<JsonObject>


    @POST("get_property_details")
    @FormUrlEncoded
    suspend fun getPropertyDetails(@Field("property_id") propertyId :Int) : Response<JsonObject>


    @POST("delete_property")
    @FormUrlEncoded
    suspend fun deleteProperty(@Field("property_id")propertyId: Int) : Response<JsonObject>

    @POST("earnings")
    @FormUrlEncoded
    suspend fun totalEarning(@Field("host_id")hostId :Int, @Field("type")type:String) : Response<JsonObject>

    @PUT("update_property_details")
    suspend fun updatePropertyDetail(@Body addProperty : PropertyDetailsSave) : Response<JsonObject>


    @POST("add_live_place")
    @FormUrlEncoded
    suspend fun addLivePlace(@Field("user_id")user_id :String,
                             @Field("place_name")place_name :String) : Response<JsonObject>

    @POST("delete_live_place")
    @FormUrlEncoded
    suspend fun deleteLivePlace(@Field("user_id")user_id :String,
                             @Field("index")index :Int) : Response<JsonObject>

    @POST("add_my_work")
    @FormUrlEncoded
    suspend fun addMyWork(@Field("user_id")user_id :String,
                          @Field("work_name")work_name :String) : Response<JsonObject>

    @POST("delete_my_work")
    @FormUrlEncoded
    suspend fun deleteMyWork(@Field("user_id")user_id :String,
                             @Field("index")work_index :Int) : Response<JsonObject>

    @POST("add_language")
    @FormUrlEncoded
    suspend fun addLanguage(@Field("user_id")user_id :String,
                            @Field("language_name")language_name :String) : Response<JsonObject>

    @POST("delete_language")
    @FormUrlEncoded
    suspend fun deleteLanguage(@Field("user_id")user_id :String,
                               @Field("index")index :Int) : Response<JsonObject>

    @POST("add_hobby")
    @FormUrlEncoded
    suspend fun addHobbies(@Field("user_id")user_id :String,
                           @Field("hobby_name")hobbies_name :String
    ) : Response<JsonObject>

    @POST("delete_hobby")
    @FormUrlEncoded
    suspend fun deleteHobbies(@Field("user_id")user_id :String,
                              @Field("index")index :Int) : Response<JsonObject>

    @POST("add_pet")
    @FormUrlEncoded
    suspend fun addPets(@Field("user_id")user_id :String,
                        @Field("pet_name")pet_name :String) : Response<JsonObject>

    @POST("delete_pet")
    @FormUrlEncoded
    suspend fun deletePets(@Field("user_id")user_id :String,
                           @Field("index")index :Int) : Response<JsonObject>

    @POST("add_street_address")
    @FormUrlEncoded
    suspend fun addStreetAddress(@Field("user_id")user_id :String,
                                 @Field("street_address")street_address :String) : Response<JsonObject>

    @POST("add_city")
    @FormUrlEncoded
    suspend fun addCity(@Field("user_id")user_id :String,
                        @Field("city")city :String) : Response<JsonObject>

    @POST("add_state")
    @FormUrlEncoded
    suspend fun addState(@Field("user_id")user_id :String,
                        @Field("state")state :String) : Response<JsonObject>

    @POST("add_zip_code")
    @FormUrlEncoded
    suspend fun addZipCode(@Field("user_id")user_id :String,
                        @Field("zip_code")zipCode :String) : Response<JsonObject>

    @POST("get_payment_methods")
    @FormUrlEncoded
    suspend fun getPaymentMethods(@Field("user_id")user_id :String ): Response<JsonObject>

    @POST("get_home_data_filter")
    @FormUrlEncoded
    suspend fun getFilteredHomeData(
        @Field("user_id") user_id: String?,
        @Field("latitude") latitude: String?,
        @Field("longitude") longitude: String?,
        @Field("place_type") place_type: String?,
        @Field("minimum_price") minimum_price: String?,
        @Field("maximum_price") maximum_price: String?,
        @Field("location") location: String?,
        @Field("date") date: String?,
        @Field("time") time: String?,
        @Field("people_count") people_count: String?,
        @Field("property_size") property_size: String?,
        @Field("bedroom") bedroom: String?,
        @Field("bathroom") bathroom: String?,
        @Field("instant_booking") instant_booking: String?,
        @Field("self_check_in") self_check_in: String?,
        @Field("allows_pets") allows_pets: String?,
        @Field("activities[]") activities: List<String>?,
        @Field("amenities[]") amenities: List<String>?,
        @Field("languages[]") languages: List<String>?): Response<JsonObject>


    @POST("update_password")
    @FormUrlEncoded
    suspend fun updatePassword(@Field("user_id")user_id :String,
                              @Field("password")password :String,
                              @Field("password_confirmation")password_confirmation :String) : Response<JsonObject>

    @POST("verify_identity")
    @FormUrlEncoded
    suspend fun  verifyIdentity(@Field("user_id")user_id :String,
                                @Field("identity_verify")identity_verify :String) : Response<JsonObject>

    @POST("social_login")
    @FormUrlEncoded
    suspend fun  getSocialLogin(@Field("fname")fname :String,
                                @Field("lname")lname :String,
                                @Field("email")email :String,
                                @Field("social_id")social_id :String,
                                @Field("fcm_token")fcm_token :String,
                                @Field("device_type")device_type :String) : Response<JsonObject>

    @POST("get_booking_list")
    @FormUrlEncoded
    suspend fun  bookingList(@Field("user_id")user_id :String) : Response<JsonObject>

    @POST("get_booking_details_list")
    @FormUrlEncoded
    suspend fun  bookingDetailsList(@Field("user_id")user_id :String,
                                    @Field("booking_id")booking_id :Int,
                                    @Field("latitude")latitude :String,
                                    @Field("longitude") longitude :String) : Response<JsonObject>

    @POST("review_host")
    @FormUrlEncoded
    suspend fun  getReviewPublish(@Field("user_id")user_id :String,
                                  @Field("booking_id")booking_id :String,
                                  @Field("property_id")property_id :String,
                                  @Field("response_rate")response_rate :String,
                                  @Field("communication")communication :String,
                                  @Field("on_time")on_time :String,
                                  @Field("review_message")review_message:String) : Response<JsonObject>

    @POST("get_notification_guest")
    @FormUrlEncoded
    suspend fun  getGuestNotification(@Field("user_id")user_id :String) : Response<JsonObject>

    @POST("mark_notification_read")
    @FormUrlEncoded
    suspend fun  getMarkGuestNotification(@Field("user_id")user_id :String,
                                          @Field("notification_id")notification_id :Int) : Response<JsonObject>

    @POST("remove_notification")
    @FormUrlEncoded
    suspend fun  getRemoveGuestNotification(@Field("user_id")user_id :String,
                                          @Field("notification_id")notification_id :Int) : Response<JsonObject>


    @POST("complete_profile")
    @Multipart
    suspend fun completeProfile(@Part("user_id")user_id : RequestBody,
                                @Part("first_name")first_name :RequestBody,
                                @Part("last_name")last_name :RequestBody,
                                @Part("about_me")about_me :RequestBody,
                                @Part ("where_live[]") whereLive:List<@JvmSuppressWildcards RequestBody>,
                                @Part ("works[]") works :List<@JvmSuppressWildcards RequestBody>,
                                @Part ("languages[]") languages :List<@JvmSuppressWildcards RequestBody>,
                                @Part ("hobbies[]") hobbies :List<@JvmSuppressWildcards RequestBody>,
                                @Part ("pets[]") pets :List<@JvmSuppressWildcards RequestBody>,
                                @Part("street_address")street_address :RequestBody,
                                @Part("city")city :RequestBody,
                                @Part("state")state :RequestBody,
                                @Part("zip_code")zip_code :RequestBody,
                                @Part profile_picture: MultipartBody.Part?,
                                @Part("identity_verify")identity_verify:RequestBody) : Response<JsonObject>

    @POST("email_verification")
    @FormUrlEncoded
    suspend fun emailVerification(@Field("user_id")user_id :String,
                             @Field("email")email :String) : Response<JsonObject>


    @POST("phone_verification")
    @FormUrlEncoded
    suspend fun phoneVerification(@Field("user_id")user_id :String,
                                  @Field("country_code")country_code :String,
                                  @Field("phone_number")phone_number :String) : Response<JsonObject>


    @POST("otp_verify_email_verification")
    @FormUrlEncoded
    suspend fun otpVerifyEmailVerification(@Field("user_id")user_id :String,
                                  @Field("otp")otp :String) : Response<JsonObject>

    @POST("otp_verify_phone_verification")
    @FormUrlEncoded
    suspend fun otpVerifyPhoneVerification(@Field("user_id")user_id :String,
                                           @Field("otp")otp :String) : Response<JsonObject>

    @POST("upload_profile_image")
    @Multipart
    suspend fun uploadProfileImage( @Part("user_id")user_id :RequestBody,
                                    @Part profile_image: MultipartBody.Part?) : Response<JsonObject>

    @POST("add_update_name")
    @FormUrlEncoded
    suspend fun addUpdateName(@Field("user_id")user_id :String,
                              @Field("first_name")first_name :String,
                              @Field("last_name")last_name :String) : Response<JsonObject>

    @POST("add_about_me")
    @FormUrlEncoded
    suspend fun addAboutme(@Field("user_id")user_id :String,
                              @Field("about_me")about_me :String) : Response<JsonObject>


    @POST("get_host_booking_list")
    @FormUrlEncoded
    suspend fun getHostBookingList(@Field("user_id")userid:Int): Response<JsonObject>


    @POST("delete_live_place")
    @FormUrlEncoded
    suspend fun deleteLivePlace(@Field("user_id")user_id :String, @Field("index")index :String) : Response<JsonObject>


    @POST("approve_decline_booking")
    @FormUrlEncoded
    suspend fun approveDeclineBooking(
        @Field("booking_id") bookingId :Int,
        @Field("status") status :String,
        @Field("host_message") message :String,
        @Field("declined_reason") declineReason :String,
        @Field("extension_id") extension_id :String
    ) : Response<JsonObject>


    @GET("get_privacy_policy")
    suspend fun getPrivacyPolicy():Response<JsonObject>

    @GET("get_term_condition")
    suspend fun getTermCondition():Response<JsonObject>

    @POST("feedback")
    @FormUrlEncoded
    suspend fun feedback(
        @Field("user_id") user_id : String,
        @Field("type") type : String,
        @Field("details") details : String
    ) :Response<JsonObject>

    @GET("get_faq")
    suspend fun getFaq():Response<JsonObject>



    @FormUrlEncoded
    @POST("update_email")
    suspend fun updateEmail(
        @Field("user_id") userId :Int,
        @Field("email") email :String
    ) : Response<JsonObject>

    @FormUrlEncoded
    @POST("update_phone_number")
    suspend fun updatePhoneNumber(
        @Field("user_id") userId :Int,
        @Field("phone_number") phoneNumber :String,
        @Field("country_code") countryCode :String
    ) : Response<JsonObject>

    @POST("property_image_delete")
    @FormUrlEncoded
    suspend fun propertyImageDelete(@Field("image_id")imageId :Int) : Response<JsonObject>

    @POST("host_booking_details")
    @FormUrlEncoded
    suspend fun hostBookingDetails(@Field("booking_id") bookingId:Int, @Field("latitude")latitude :String?,
                                   @Field("longitude") longitude :String?,
                                   @Field("extension_id") extension_id:String?) : Response<JsonObject>

    @POST("contact_us")
    @FormUrlEncoded
    suspend fun contactUs(
        @Field("user_id") user_id : String,
        @Field("name") name : String,
        @Field("email") email : String,
        @Field("message") message : String,
    ) :Response<JsonObject>


    @POST("get_help_center")
    @FormUrlEncoded
    suspend fun getHelpCenter(
        @Field("user_id") user_id : String,
        @Field("user_type") user_type : String
    ) :Response<JsonObject>

    @POST("get_article_details")
    @FormUrlEncoded
    suspend fun getArticleDetails(
        @Field("article_id") article_id : String
    ) :Response<JsonObject>

    @POST("get_guide_details")
    @FormUrlEncoded
    suspend fun getGuideDetails(
        @Field("guide_id") guide_id : String
    ) :Response<JsonObject>


    @POST("get_home_data")
    @FormUrlEncoded
    suspend fun getHomeData(
        @Field("user_id") user_id : String,
        @Field("latitude") latitude : String,
        @Field("longitude") longitude : String
    ) :Response<JsonObject>

    @POST("get_wishlist")
    @FormUrlEncoded
    suspend fun getWisList(
        @Field("user_id") user_id : String) :Response<JsonObject>

    @POST("create_wishlist")
    @FormUrlEncoded
    suspend fun createWishlist(
        @Field("user_id") user_id : String,
        @Field("name") name : String,
        @Field("description") description : String,
        @Field("property_id") property_id : String) :Response<JsonObject>

    @POST("delete_wishlist")
    @FormUrlEncoded
    suspend fun deleteWishlist(
        @Field("user_id") user_id : String,
        @Field("wishlist_id") wishlist_id : String) :Response<JsonObject>

    @POST("remove_item_from_wishlist")
    @FormUrlEncoded
    suspend fun removeItemFromWishlist(
        @Field("user_id") user_id : String,
        @Field("property_id") property_id : String) :Response<JsonObject>


    @POST("save_item_in_wishlist")
    @FormUrlEncoded
    suspend fun saveItemInWishlist(
        @Field("user_id") user_id : String,
        @Field("property_id") property_id : String,
        @Field("wishlist_id") wishlist_id : String) :Response<JsonObject>




    @POST("filter_property_reviews")
    @FormUrlEncoded
    suspend fun propertyFilterReviews(
        @Field("property_id") propertyId :Int,
        @Field("filter") filter: String,
        @Field("page") page :Int
    ) : Response<JsonObject>

    @POST("get_notification_host")
    @FormUrlEncoded
    suspend fun getNotificationHost(@Field("user_id") userId :Int) : Response<JsonObject>


    @POST("mark_notification_read")
    @FormUrlEncoded
    suspend fun deleteNotificationHost(@Field("user_id") userId :Int,@Field("notification_id") notificationId:Int) : Response<JsonObject>


    @POST("host_report_violation")
    @FormUrlEncoded
    suspend fun hostReportViolation(
        @Field("user_id") userId :Int, @Field("booking_id") bookingId :Int, @Field("property_id") propertyId :Int,
        @Field("report_reasons_id")reportReasonId :Int,
        @Field("additional_details") additionalDetails :String
    ) : Response<JsonObject>

    @GET("list_report_reasons")
    suspend fun reportListReason() : Response<JsonObject>

    @POST("get_article_list")
    @FormUrlEncoded
    suspend fun getArticleList(@Field("search_term") search_term : String,
                               @Field("user_type") user_type : String):Response<JsonObject>

    @POST("get_guide_list")
    @FormUrlEncoded
    suspend fun getGuideList(@Field("search_term") search_term : String,
                             @Field("user_type") user_type : String):Response<JsonObject>


    @POST("host_report_violation")
    @FormUrlEncoded
    suspend fun hostReportViolationSend(@Field("user_id")userId :Int,
                                    @Field("booking_id") bookingId :Int,
                                    @Field("property_id") propertyId :Int,
                                    @Field("report_reasons_id")reportReasonId :Int,
                                    @Field("additional_details")additionalDetail :String) : Response<JsonObject>

    @POST("get_home_property_details")
    @FormUrlEncoded
    suspend fun getHomePropertyDetails(
        @Field("user_id") user_id : String,
        @Field("property_id") property_id : String) :Response<JsonObject>

    @POST("filter_property_reviews")
    @FormUrlEncoded
    suspend fun filterPropertyReviews(
        @Field("property_id") property_id : String,
        @Field("filter") filter : String,
        @Field("page") page : String) :Response<JsonObject>

    @POST("get_user_cards")
    @FormUrlEncoded
    suspend fun getUserCards(
        @Field("user_id") userId : String) :Response<JsonObject>


    @POST("same_as_mailing_address")
    @FormUrlEncoded
    suspend fun sameAsMailingAddress(
        @Field("user_id") userId : String) :Response<JsonObject>

    @POST("save_card_stripe")
    @FormUrlEncoded
    suspend fun saveCardStripe(
        @Field("user_id") userId : String,
        @Field("token_stripe") token_stripe : String) :Response<JsonObject>


    @POST("set_preferred_card")
    @FormUrlEncoded
    suspend fun setPreferredCard(
        @Field("user_id") userId : String,
        @Field("card_id") token_stripe : String) :Response<JsonObject>

    @POST("book_property")
    @FormUrlEncoded
    suspend fun bookProperty(
        @Field("user_id") userId : String,
        @Field("property_id") property_id : String,
        @Field("booking_date") booking_date : String,
        @Field("booking_start") booking_start : String,
        @Field("booking_end") booking_end : String,
        @Field("booking_amount") booking_amount : String,
        @Field("total_amount") total_amount : String,
        @Field("customer_id") customer_id : String,
        @Field("card_id") card_id : String,
        @FieldMap addons: Map<String, String>,
        @Field("service_fee") service_fee : String,
        @Field("tax") tax : String,
        @Field("discount_amount") discount_amount : String) :Response<JsonObject>

    @POST("report_violation")
    @FormUrlEncoded
    suspend fun reportViolation(
        @Field("user_id") userId : String,
        @Field("booking_id") booking_id : String,
        @Field("property_id") property_id : String,
        @Field("report_reasons_id") report_reasons_id : String,
        @Field("additional_details") additional_details : String) :Response<JsonObject>


    @GET("list_report_reasons")
    suspend fun listReportReasons() :Response<JsonObject>

    @POST("guest_cancel_booking")
    @FormUrlEncoded
    suspend fun cancelBooking(
        @Field("user_id") userId : String,
        @Field("booking_id") booking_id : String) :Response<JsonObject>


    @POST("logout")
    @FormUrlEncoded
    suspend fun logout(
        @Field("user_id") user_id : String) :Response<JsonObject>


    @POST("review_guest")
    @FormUrlEncoded
    suspend fun reviewGuest(
                 @Field("user_id") userId :Int,
                 @Field("booking_id") bookingId :Int,
                 @Field("property_id") propertyId :Int,
                 @Field("response_rate") responseRate :Int,
                 @Field("communication") communication :Int,
                 @Field("on_time") onTime :Int,
                 @Field("review_message") reviewMessage :String
     ) : Response<JsonObject>

    @POST("chat_token")
    @FormUrlEncoded
    suspend fun getChatToken(@Field("user_id") user_Id:Int,@Field("role") role :String) : Response<JsonObject>

    @POST("property_booking_details")
    @FormUrlEncoded
    suspend fun propertyBookingDetails(
        @Field("property_id") property_id : String,
        @Field("user_id") user_id : String,
        @Field("start_date") start_date : String,
        @Field("end_date") end_date : String,
        @Field("latitude") latitude : String,
        @Field("longitude") longitude : String,
    ) :Response<JsonObject>

    @POST("toggle_property_booking")
    @FormUrlEncoded
    suspend fun togglePropertyBooking(
        @Field("property_id") property_id : String,
        @Field("user_id") user_id : String
    ) :Response<JsonObject>



    @POST("join_channel")
    @FormUrlEncoded
    suspend fun joinChatChannel(
        @Field("senderId") senderId :Int,
        @Field("receiverId") receiverId :Int,
        @Field("groupChannel") groupChannel :String,
        @Field("userType") user_type: String
    ) : Response<JsonObject>
    

    @Multipart
    @POST("add_payout_bank")
    suspend fun addPayoutBank(
        @Part("user_id") user_id: RequestBody,
        @Part("first_name") first_name: RequestBody,
        @Part("last_name") last_name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone_number") phone_number: RequestBody,
        @Part dobList: List<MultipartBody.Part>,
        @Part("id_type") id_type: RequestBody,
        @Part("ssn_last_4") ssn_last_4: RequestBody,
        @Part("id_number") id_number: RequestBody,
        @Part("address") address: RequestBody,
        @Part("country") country: RequestBody,
        @Part("state") state: RequestBody,
        @Part("city") city: RequestBody,
        @Part("postal_code") postal_code: RequestBody,
        @Part("bank_name") bank_name: RequestBody,
        @Part("account_holder_name") account_holder_name: RequestBody,
        @Part("account_number") account_number: RequestBody,
        @Part("account_number_confirmation") account_number_confirmation: RequestBody,
        @Part("routing_number") routing_number: RequestBody,
        @Part("bank_proof_type") bankProofType : RequestBody,
        @Part bank_proof_document: MultipartBody.Part?,
        @Part verification_document_front: MultipartBody.Part?,
        @Part verification_document_back: MultipartBody.Part?
    ): Response<JsonObject>



    @POST("get_user_channels")
    @FormUrlEncoded
    suspend fun getUserChannel(
          @Field("user_id") userId :Int,
          @Field("type") type:String,
          @Field("archive_status") archive_status:String
    ) : Response<JsonObject>


    @GET("get_countries")
    suspend fun getCountries() : Response<JsonObject>

    @GET("get_states/{value}")
    suspend fun getValue(@Path("value") value: String): Response<JsonObject>

    @GET("get_cities/{value}/{value1}")
    suspend fun getCityName(@Path("value") country:String, @Path("value1") state :String) : Response<JsonObject>

    @POST("filter_property_reviews")
    @FormUrlEncoded
    suspend fun filterPropertyReviewsHost(
        @Field("property_id") propertyId :Int,
        @Field("filter") filter :String,
        @Field("page") page :Int
    ) : Response<JsonObject>


    @POST("get_booking_extension_time_amount")
    @FormUrlEncoded
    suspend fun getBookingExtensionTimeAmount(
        @Field("user_id") userId :String,
        @Field("booking_id") booking_id:String,
        @Field("extension_time") extension_time:String,
        @Field("service_fee") service_fee:String,
        @Field("tax") tax:String,
        @Field("cleaning_fee") cleaning_fee:String,
        @Field("extension_total_amount") extension_total_amount:String,
        @Field("extension_booking_amount") extension_booking_amount:String,
        @Field("discount_amount") discount_amount:String
    ) : Response<JsonObject>

    @POST("get_home_data")
    @FormUrlEncoded
    suspend fun getHomeDataSearchFilter(
        @Field("user_id") user_id : String,
        @Field("latitude") latitude : String,
        @Field("longitude") longitude : String,
        @Field("date") date : String,
        @Field("hour") hour : String,
        @Field("start_time") start_time : String,
        @Field("end_time") end_time : String,
        @Field("activity") activity : String
    ) :Response<JsonObject>

    @POST("get_user_bookings")
    @FormUrlEncoded
    suspend fun getUserBookings(
        @Field("user_id") user_id : String,
        @Field("booking_date") booking_date : String,
        @Field("booking_start") booking_start : String
    ) :Response<JsonObject>


    @Multipart
    @POST("add_payout_card")
    suspend fun addPayoutCard(
        @Part("user_id") user_id: RequestBody,
        @Part("token") token: RequestBody,
        @Part("first_name") first_name: RequestBody,
        @Part("last_name") last_name: RequestBody,
        @Part("email") email: RequestBody,
        @Part dobList: List<MultipartBody.Part>,
        @Part("ssn_last_4") ssn_last_4: RequestBody,
        @Part("phone_number") phone_number: RequestBody,
        @Part("address") address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("state") state: RequestBody,
        @Part("country") country: RequestBody,
        @Part("postal_code") postal_code: RequestBody,
        @Part("id_type") id_type: RequestBody,
        @Part("id_number") id_number: RequestBody,
        @Part verification_document_front: MultipartBody.Part?,
        @Part verification_document_back: MultipartBody.Part?
    ): Response<JsonObject>


    @POST("get_payout_methods")
    @FormUrlEncoded
    suspend fun getPayoutMethods(
        @Field("user_id") userId :String
    ) : Response<JsonObject>

    @POST("set_primary_payout_method")
    @FormUrlEncoded
    suspend fun setPrimaryPayoutMethod(
        @Field("user_id") userId :String,
        @Field("payout_method_id") payoutMethodId :String
    ) : Response<JsonObject>

    @POST("delete_payout_method")
    @FormUrlEncoded
    suspend fun deletePayoutMethod(
        @Field("user_id") userId :String,
        @Field("payout_method_id") payoutMethodId :String
    ) : Response<JsonObject>


    @POST("host_unread_bookings")
    @FormUrlEncoded
    suspend fun getHostUnreadBookings(
        @Field("user_id") userId :Int
    ) : Response<JsonObject>


    @POST("mark_host_bookings")
    @FormUrlEncoded
    suspend fun markHostBooking(
        @Field("user_id") userId :Int
    ) : Response<JsonObject>



    @POST("payment_withdrawal_list")
    @FormUrlEncoded
    suspend fun paymentWithdrawalList(
        @Field("user_id") userId :String,
        @Field("start_date") start_date :String,
        @Field("end_date") end_date :String,
        @Field("filter_status") filter_status :String,
    ) : Response<JsonObject>

    @POST("payout_balance")
    @FormUrlEncoded
    suspend fun payoutBalance(
        @Field("user_id") userId :String
    ) : Response<JsonObject>

    @POST("request_withdrawal")
    @FormUrlEncoded
    suspend fun requestWithdrawal(
        @Field("user_id") userId :String,
        @Field("amount") amount :String,
        @Field("withdrawal_type") withdrawalType :String,
    ) : Response<JsonObject>


    @POST("get_saved_item_wishlist")
    @FormUrlEncoded
    suspend fun getSavedItemWishList(
        @Field("user_id") userId :Int,
        @Field("wishlist_id") wishListId :Int,
        @Field("latitude") latitude : String ,
        @Field("longitude")   longitude : String
    ) : Response<JsonObject>


    @POST("otp_verify_update_phone")
    @FormUrlEncoded
    suspend fun otpVerifyUpdatePhoneNumber(
        @Field("user_id") userId :Int,
        @Field("otp") otp :String
    ) : Response<JsonObject>

    @POST("block_user")
    @FormUrlEncoded
    suspend fun blockUser(
        @Field("senderId") userId :Int,
        @Field("group_channel") group_channel :String,
        @Field("blockUnblock") blockUnblock :Int,
    ) : Response<JsonObject>


    @POST("mark_favorite_chat")
    @FormUrlEncoded
    suspend fun markFavoriteChat(
        @Field("senderId") userId :Int,
        @Field("group_channel") group_channel :String,
        @Field("favorite") favorite :Int,
    ) : Response<JsonObject>

    @POST("send_chat_notification")
    @FormUrlEncoded
    suspend fun sendChatNotification(
        @Field("sender_id") sender_id :String,
        @Field("receiver_id") receiver_id :String,
        @Field("group_channel") group_channel :String
    ) : Response<JsonObject>

    @POST("mute_chat")
    @FormUrlEncoded
    suspend fun muteChat(
        @Field("user_id") userId :Int,
        @Field("group_channel") group_channel :String,
        @Field("mute") mute :Int,
    ) : Response<JsonObject>


    @POST("toggle_archive_unarchive")
    @FormUrlEncoded
    suspend fun toggleArchiveUnarchive(
        @Field("user_id") userId :Int,
        @Field("group_channel") group_channel :String,
    ) : Response<JsonObject>



    @POST("otp_verify_update_email")
    @FormUrlEncoded
    suspend fun otpVerifyUpdateEmail(
        @Field("user_id") userId :Int,
        @Field("otp") otp :String
    ) : Response<JsonObject>

    @POST("withdraw_funds")
    @FormUrlEncoded
    suspend fun withdrawFunds(
        @Field("user_id") userId :String
    ) : Response<JsonObject>

    @POST("report_chat")
    @FormUrlEncoded
    suspend fun reportChat(
        @Field("reporter_id") reporter_id :String,
        @Field("reported_user_id") reported_user_id :String,
        @Field("report_reasons_id") reason :String,
        @Field("message") message :String,
        @Field("group_channel") group_channel :String,
    ) : Response<JsonObject>

    @POST("otp_reset_password")
    @FormUrlEncoded
    suspend fun otpResetPassword(
        @Field("user_id") userId :Int
    ) : Response<JsonObject>


    @POST("host_listing")
    @FormUrlEncoded
    suspend fun hostListing(
        @Field("host_id") hostId :String,
        @Field("latitude") latitude :String,
        @Field("longitude") longitude :String
    ) : Response<JsonObject>

    @POST("filter_host_reviews")
    @FormUrlEncoded
    suspend fun filterHostReviews(
        @Field("host_id") host_id :String,
        @Field("latitude") latitude :String,
        @Field("longitude") longitude :String,
        @Field("filter") filter: String,
        @Field("page") page :String
    ) : Response<JsonObject>

    @POST("delete_chat")
    @FormUrlEncoded
    suspend fun deleteChat(
        @Field("user_id") user_id :String,
        @Field("user_type") user_type :String,
        @Field("group_channel") group_channel :String
    ) : Response<JsonObject>


    @POST("check_host_property_availability")
    @FormUrlEncoded
    suspend fun checkHostPropertyAvailability(
        @Field("property_id") property_id :String,
        @Field("start_time") start_time :String,
        @Field("end_time") end_time :String
    ) : Response<JsonObject>

}